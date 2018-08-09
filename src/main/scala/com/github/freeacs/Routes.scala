package com.github.freeacs

import java.io.UnsupportedEncodingException
import java.net.URLDecoder

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.actors.ConversationActor
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Routes(breaker: CircuitBreaker,
             services: Tr069Services,
             authService: AuthenticationService,
             configuration: Configuration)
            (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives {

  val log = LoggerFactory.getLogger(getClass)

  def routes: Route =
    post {
      path("tr069") {
        logRequestResult("tr069") {
          extractClientIP { remoteIp =>
            authenticateConversation(remoteIp.toString(), (u) =>
              entity(as[SOAPRequest]) { soapRequest =>
                complete(handle(soapRequest, u))
              })
          }
        }
      }
    }

  def authenticateConversation(remoteIp: String, route: (String) => Route) =
    extractCredentials {
      case Some(credentials) =>
        def failed(e: Throwable) = {
          log.error("Failed to retrieve/check user secret", e)
          HttpResponse(StatusCodes.InternalServerError).withEntity("Failed to retrieve/check user secret")
        }
        credentials.scheme() match {
          case "Basic" =>
            val (username, password) = decodeBasicAuth(credentials.token())
            onComplete(authService.authenticator(username, _.equals(password))) {
              case Success(Right(_)) =>
                route(username)
              case Success(Left(_)) =>
                complete(unauthorizedBasic)
              case Failure(e) =>
                complete(failed(e))
            }
          case "Digest" =>
            val username = username2unitId(credentials.params("username"))
            val nonce = credentials.params("nonce")
            val nc = credentials.params("nc")
            val cnonce = credentials.params("cnonce")
            val qop = credentials.params("qop")
            val uri = credentials.params("uri")
            val response = credentials.params("response")
            val method = "POST"
            onComplete(authService.authenticator(username, verifyDigest(username, method, uri, nonce, nc, cnonce, qop, response))) {
              case Success(Right((_, _))) =>
                route(username)
              case Success(Left(_)) =>
                complete(unauthorizedDigest(remoteIp))
              case Failure(e) =>
                complete(failed(e))
            }
        }
      case None =>
        if (configuration.authMethod.toLowerCase.equals("basic"))
          complete(unauthorizedBasic)
        else
          complete(unauthorizedDigest(remoteIp))
    }

  def verifyDigest(username: String, method: String, uri: String, nonce: String, nc: String, cnonce: String, qop: String, response: String)(secret: String): Boolean = {
    val sharedSecret = {
      if (secret != null && secret.length > 16 && !(passwordMd5(username, secret, method, uri, nonce, nc, cnonce, qop) == response))
        secret.substring(0, 16)
      else
        secret
    }
    passwordMd5(username, sharedSecret, method, uri, nonce, nc, cnonce, qop).equals(response)
  }

  def passwordMd5(username: String, password: String, method: String, uri: String, nonce: String, nc: String, cnonce: String, qop: String): String = {
    val realm = configuration.digestRealm
    val a1 = username + ":" + realm + ":" + password
    val md5a1 = DigestUtils.md5Hex(a1)
    val a2 = method + ":" + uri
    val md5a2 = DigestUtils.md5Hex(a2)
    val a3 = md5a1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + md5a2
    val md5a3 = DigestUtils.md5Hex(a3)
    md5a3
  }

  /**
    * Convert the authentication username to unitid (should be 1:1, but there might be some
    * vendor specific problems to solve...
    *
    * @throws UnsupportedEncodingException thrown if url decoding fails
    */
  def username2unitId(username: String): String = {
    try
      URLDecoder.decode(username, "UTF-8")
    catch {
      case _: UnsupportedEncodingException =>
        username
    }
  }

  private def getDigestHeader(nonce: String): RawHeader = {
    val realm = configuration.digestRealm
    val authenticateHeader = "Digest realm=\"" + realm + "\", " + "qop=\"" + configuration.digestQop + "\", nonce=\"" + nonce + "\", " + "opaque=\"" + DigestUtils.md5Hex(nonce) + "\""
    RawHeader("WWW-Authenticate", authenticateHeader)
  }

  def decodeBasicAuth(authHeader: String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
    val decoded = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }

  def unauthorizedBasic =
    HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = immutable.Seq(RawHeader("WWW-Authenticate", s"""Basic realm="${configuration.basicRealm}""""))
    )

  def unauthorizedDigest(remoteIp: String) = {
    log.warn("Unauthorized")
    val now = System.currentTimeMillis
    HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = immutable.Seq(getDigestHeader(DigestUtils.md5Hex(remoteIp + ":" + now + ":" + configuration.digestSecret)))
    )
  }

  def handle(soapRequest: SOAPRequest, user: String): Future[ToResponseMarshallable] = {
    implicit val timeout: Timeout = configuration.responseTimeout
    breaker.withCircuitBreaker(
      getConversationActor(user).flatMap(_ ? soapRequest)
    ).map(_.asInstanceOf[SOAPResponse])
      .map[ToResponseMarshallable] { response =>
      Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
        case Right(elm) =>
          makeHttpResponse(StatusCodes.OK, MediaTypes.`text/xml`, Some(elm.toString()))
        case Left(InvalidRequest) =>
          makeHttpResponse(StatusCodes.BadRequest, MediaTypes.`text/plain`, Some("Invalid request"))
        case _ =>
          makeHttpResponse(StatusCodes.OK, MediaTypes.`text/plain`, None)
      }
    }.recover {
      case _: CircuitBreakerOpenException =>
        Future.successful(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case e: Throwable =>
        Future.successful(HttpResponse(StatusCodes.InternalServerError))
    }
  }

  def makeHttpResponse(status: StatusCode, charset: MediaType.WithOpenCharset, str: Option[String]) =
    HttpResponse(
      status = status,
      entity = HttpEntity.CloseDelimited(
        ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
        Source.single(str.map(ByteString.apply).getOrElse(ByteString.empty))
      )
    )

  def getConversationActor(user: String): Future[ActorRef] =
    system.actorSelection(s"user/conversation-$user")
      .resolveOne(configuration.actorTimeout)
      .recover { case _: Exception =>
        system.actorOf(ConversationActor.props(user, services), s"conversation-$user")
      }

}