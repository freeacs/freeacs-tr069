package com.github.freeacs

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{ask, CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.actors.ConversationActor
import com.github.freeacs.auth.{BasicAuthorization, DigestAuthorization}
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Routes(
    breaker: CircuitBreaker,
    services: Tr069Services,
    authService: AuthenticationService,
    configuration: Configuration
)(implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
    extends Directives {

  val log = LoggerFactory.getLogger(getClass)

  def routes: Route =
    get {
      path("health") {
        complete(s"${configuration.name} OK")
      }
    } ~
      post {
        path("tr069") {
          logRequestResult("tr069") {
            extractClientIP { remoteIp =>
              authenticateConversation(
                remoteIp.toIP.map(_.ip.getHostAddress).getOrElse("Unknown"),
                (username) =>
                  entity(as[SOAPRequest]) { soapRequest =>
                    complete(handle(soapRequest, username))
                }
              )
            }
          }
        }
      }

  type Verifier = String => Boolean

  def authenticateConversation(remoteIp: String, route: (String) => Route) =
    extractCredentials {
      case Some(credentials) =>
        def failed(e: Throwable) = {
          log.error("Failed to retrieve/check user secret", e)
          HttpResponse(StatusCodes.InternalServerError)
            .withEntity("Failed to retrieve/check user secret")
        }
        credentials.scheme() match {
          case "Basic" =>
            val (username, password) =
              BasicAuthorization.decodeBasicAuth(credentials.token())
            val verifier: Verifier = _.equals(password)
            onComplete(authService.authenticator(username, verifier)) {
              case Success(Right(_)) =>
                route(username)
              case Success(Left(_)) =>
                complete(
                  BasicAuthorization.unauthorizedBasic(configuration.basicRealm)
                )
              case Failure(e) =>
                complete(failed(e))
            }
          case "Digest" =>
            val username = DigestAuthorization.username2unitId(
              credentials.params("username")
            )
            val verifier: Verifier = DigestAuthorization.verifyDigest(
              username,
              credentials.params,
              configuration.digestRealm
            )
            onComplete(authService.authenticator(username, verifier)) {
              case Success(Right((_, _))) =>
                route(username)
              case Success(Left(_)) =>
                complete(
                  DigestAuthorization.unauthorizedDigest(
                    remoteIp,
                    configuration.digestRealm,
                    configuration.digestQop,
                    configuration.digestSecret
                  )
                )
              case Failure(e) =>
                complete(failed(e))
            }
        }
      case None =>
        if (configuration.authMethod.toLowerCase.equals("basic"))
          complete(
            BasicAuthorization.unauthorizedBasic(configuration.basicRealm)
          )
        else
          complete(
            DigestAuthorization.unauthorizedDigest(
              remoteIp,
              configuration.digestRealm,
              configuration.digestQop,
              configuration.digestSecret
            )
          )
    }

  def handle(
      soapRequest: SOAPRequest,
      user: String
  ): Future[ToResponseMarshallable] = {
    implicit val timeout: Timeout = configuration.responseTimeout
    breaker
      .withCircuitBreaker(
        getConversationActor(user).flatMap(_ ? soapRequest)
      )
      .map(_.asInstanceOf[SOAPResponse])
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeHttpResponse(
              StatusCodes.OK,
              MediaTypes.`text/xml`,
              Some(elm.toString())
            )
          case Left(InvalidRequest) =>
            makeHttpResponse(
              StatusCodes.BadRequest,
              MediaTypes.`text/plain`,
              Some("Invalid request")
            )
          case _ =>
            makeHttpResponse(StatusCodes.OK, MediaTypes.`text/plain`, None)
        }
      }
      .recover {
        case _: CircuitBreakerOpenException =>
          Future.successful(
            HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy")
          )
        case e: Throwable =>
          Future.successful(HttpResponse(StatusCodes.InternalServerError))
      }
  }

  def makeHttpResponse(
      status: StatusCode,
      charset: MediaType.WithOpenCharset,
      str: Option[String]
  ) =
    HttpResponse(
      status = status,
      entity = HttpEntity.CloseDelimited(
        ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
        Source.single(str.map(ByteString.apply).getOrElse(ByteString.empty))
      )
    )

  def getConversationActor(user: String): Future[ActorRef] =
    system
      .actorSelection(s"user/conversation-$user")
      .resolveOne(configuration.actorTimeout)
      .recover {
        case _: Exception =>
          system.actorOf(
            ConversationActor.props(user, services),
            s"conversation-$user"
          )
      }

}
