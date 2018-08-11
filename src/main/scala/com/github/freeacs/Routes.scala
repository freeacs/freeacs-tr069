package com.github.freeacs

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directive, Directives, Route}
import akka.pattern.{ask, CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.actors.SessionManager.{
  CreateSession,
  GetSessionIds,
  SessionIds
}
import com.github.freeacs.actors.{
  ConversationActor,
  NonceActor,
  NonceCount,
  SessionId
}
import com.github.freeacs.auth.{BasicAuthorization, DigestAuthorization}
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Routes(
    breaker: CircuitBreaker,
    services: Tr069Services,
    authService: AuthenticationService,
    config: Configuration,
    replicator: ActorRef
)(implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
    extends Directives {

  val log = LoggerFactory.getLogger(getClass)

  def routes: Route =
    get {
      path("health") {
        complete(s"${config.name} OK")
      }
    } ~
      post {
        path("tr069") {
          logRequestResult("tr069") {
            extractClientIP { remoteIp =>
              authenticateConversation(
                remoteIp.toIP.map(_.ip.getHostAddress).getOrElse("Unknown"),
                (username, conversationActor) =>
                  entity(as[SOAPRequest]) { soapRequest =>
                    complete(handle(soapRequest, username, conversationActor))
                }
              )
            }
          }
        }
      }

  def failed(e: Throwable) = {
    log.error("Failed", e)
    HttpResponse(StatusCodes.InternalServerError)
  }

  type Verifier = String => Future[Boolean]

  def authenticateConversation(
      remoteIp: String,
      route: (String, ActorRef) => Route
  ) =
    extractCredentials {
      case Some(credentials) =>
        credentials.scheme() match {
          case "Basic" =>
            val (username, password) =
              BasicAuthorization.decodeBasicAuth(credentials.token())
            val verifier: Verifier = p => Future.successful(p.equals(password))
            onComplete(authService.authenticator(username, verifier)) {
              case Success(Right(_)) =>
                onComplete(
                  ConversationActor.getConversationActor(
                    username,
                    services,
                    config.actorTimeout
                  )
                ) {
                  case Success(conversationActor) =>
                    route(username, conversationActor)
                  case Failure(e) =>
                    complete(failed(e))
                }
              case Success(Left(_)) =>
                complete(
                  BasicAuthorization.unauthorizedBasic(config.basicRealm)
                )
              case Failure(e) =>
                complete(failed(e))
            }
          case "Digest" =>
            val username = DigestAuthorization.username2unitId(
              credentials.params("username")
            )
            onComplete(
              ConversationActor.getConversationActor(
                username,
                services,
                config.actorTimeout
              )
            ) {
              case Success(conversationActor) =>
                conversationActor ! NonceCount(nc = credentials.params("nc"))
                onComplete(NonceActor.getNonceActor(config.actorTimeout)) {
                  case Success(actorRef) =>
                    val verifier: Verifier = DigestAuthorization.verifyDigest(
                      username,
                      credentials.params,
                      config.digestRealm,
                      actorRef,
                      config.nonceTTL
                    )
                    onComplete(authService.authenticator(username, verifier)) {
                      case Success(Right(())) =>
                        replicator ! CreateSession(SessionId())
                        implicit val timeout: Timeout =
                          FiniteDuration(1, TimeUnit.SECONDS)
                        onComplete(
                          (replicator ? GetSessionIds)
                            .map(_.asInstanceOf[SessionIds])
                        ) {
                          case Success(value) =>
                            log.info(
                              "There is currently {} sessionIds in SessionManager",
                              value.ids.size
                            )
                            route(username, conversationActor)
                          case Failure(exception) =>
                            complete(failed(exception))
                        }
                      case Success(Left(_)) =>
                        complete(
                          DigestAuthorization.unauthorizedDigest(
                            remoteIp,
                            config.digestRealm,
                            config.digestQop,
                            config.digestSecret,
                            actorRef
                          )
                        )
                      case Failure(exception) =>
                        complete(failed(exception))
                    }
                  case Failure(e) =>
                    complete(failed(e))
                }
              case Failure(exception) =>
                complete(failed(exception))
            }
        }
      case None =>
        if (config.authMethod.toLowerCase.equals("basic"))
          complete(
            BasicAuthorization.unauthorizedBasic(config.basicRealm)
          )
        else
          onComplete(NonceActor.getNonceActor(config.actorTimeout)) {
            case Success(actorRef) =>
              complete(
                DigestAuthorization.unauthorizedDigest(
                  remoteIp,
                  config.digestRealm,
                  config.digestQop,
                  config.digestSecret,
                  actorRef
                )
              )
            case Failure(e) =>
              complete(failed(e))
          }

    }

  def handle(
      soapRequest: SOAPRequest,
      user: String,
      conversationActor: ActorRef
  ): Future[ToResponseMarshallable] = {
    implicit val timeout: Timeout = config.responseTimeout
    breaker
      .withCircuitBreaker(
        conversationActor ? soapRequest
      )
      .map(_.asInstanceOf[SOAPResponse])
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeHttpResponse(
              StatusCodes.OK,
              MediaTypes.`text/xml`,
              config.mode,
              Some(elm.toString())
            )
          case Left(InvalidRequest) =>
            makeHttpResponse(
              StatusCodes.BadRequest,
              MediaTypes.`text/plain`,
              config.mode,
              Some("Invalid request")
            )
          case _ =>
            makeHttpResponse(
              StatusCodes.OK,
              MediaTypes.`text/plain`,
              config.mode,
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
          Future.successful(failed(e))
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
          payload
            .map(p => {
              HttpEntity.Chunked(
                contentType,
                Source.single(ChunkStreamPart(p))
              )
            })
            .getOrElse(HttpEntity.Empty)
        case "delimited" =>
          payload
            .map(p => {
              HttpEntity.CloseDelimited(
                contentType,
                Source.single(ByteString(p))
              )
            })
            .getOrElse(HttpEntity.Empty)
      }
    )
  }

}
