package com.github.freeacs

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{ask, CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.actors.{ConversationActor, NonceCount}
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
                (username, actor) =>
                  entity(as[SOAPRequest]) { soapRequest =>
                    complete(handle(soapRequest, username, actor))
                }
              )
            }
          }
        }
      }

  type Verifier = String => Boolean

  def authenticateConversation(
      remoteIp: String,
      route: (String, ActorRef) => Route
  ) =
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
                onComplete(getConversationActor(username)) {
                  case Success(actor) =>
                    route(username, actor)
                  case Failure(_) =>
                    complete(
                      HttpResponse(StatusCodes.InternalServerError)
                    )
                }
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
            onComplete(getConversationActor(username)) {
              case Success(actor) =>
                actor ! NonceCount(nc = credentials.params("nc"))
                val verifier: Verifier = DigestAuthorization.verifyDigest(
                  username,
                  credentials.params,
                  configuration.digestRealm
                )
                onComplete(authService.authenticator(username, verifier)) {
                  case Success(Right((_, _))) =>
                    route(username, actor)
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
              case Failure(exception) =>
                complete(failed(exception))
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
      user: String,
      actor: ActorRef
  ): Future[ToResponseMarshallable] = {
    implicit val timeout: Timeout = configuration.responseTimeout
    breaker
      .withCircuitBreaker(
        actor ? soapRequest
      )
      .map(_.asInstanceOf[SOAPResponse])
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeHttpResponse(
              StatusCodes.OK,
              MediaTypes.`text/xml`,
              configuration.mode,
              Some(elm.toString())
            )
          case Left(InvalidRequest) =>
            makeHttpResponse(
              StatusCodes.BadRequest,
              MediaTypes.`text/plain`,
              configuration.mode,
              Some("Invalid request")
            )
          case _ =>
            makeHttpResponse(
              StatusCodes.OK,
              MediaTypes.`text/plain`,
              configuration.mode,
              None
            )
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
      mode: String,
      payload: Option[String]
  ) = {
    val contentType = ContentType.WithCharset(charset, HttpCharsets.`UTF-8`)
    HttpResponse(
      status = status,
      entity = mode match {
        case "chunked" =>
          HttpEntity.Chunked(
            contentType,
            Source.single(
              payload
                .map(ChunkStreamPart.apply)
                .getOrElse(ChunkStreamPart(ByteString.empty))
            )
          )
        case "delimited" =>
          HttpEntity.CloseDelimited(
            contentType,
            Source.single(
              payload.map(ByteString.apply).getOrElse(ByteString.empty)
            )
          )
      }
    )
  }

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
