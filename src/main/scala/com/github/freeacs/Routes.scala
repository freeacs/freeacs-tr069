package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.github.freeacs.Implicits._
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.session.SessionService
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._
import com.github.jarlah.authenticscala.Authenticator._
import com.github.jarlah.authenticscala.{
  AuthenticationContext,
  AuthenticationResult
}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Routes(
    breaker: CircuitBreaker,
    services: Tr069Services,
    authService: AuthenticationService,
    config: Configuration,
    conversation: SessionService
)(implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
    extends Directives {

  val log = LoggerFactory.getLogger(getClass)

  def extractAuthenticationContext(
      route: AuthenticationContext => Route
  ): Route =
    (extractRequest & extractClientIP) { (request, remoteIp) =>
      val context: AuthenticationContext = request
      route(
        context.copy(
          remoteAddress =
            remoteIp.toIP.map(_.ip.getHostAddress).getOrElse("Unknown")
        )
      )
    }

  def routes: Route =
    get {
      path("health") {
        complete(s"${config.name} OK")
      }
    } ~
      post {
        path("tr069") {
          logRequestResult("tr069") {
            authenticateConversation { user =>
              entity(as[SOAPRequest]) { soapRequest =>
                complete(handle(soapRequest, user))
              }
            }
          }
        }
      }

  def authenticateConversation(route: (String) => Route): Route =
    extractAuthenticationContext { (context) =>
      onComplete(
        authenticate(context, authService.getSecret, config.authMethod)
      ) {
        case Success(AuthenticationResult(success, maybeUser, maybeError)) =>
          if (success && maybeUser.isDefined) {
            route(maybeUser.get)
          } else {
            complete(
              HttpResponse(Unauthorized, challenge(context, config.authMethod))
                .withEntity(maybeError.getOrElse(""))
            )
          }
        case Failure(_) =>
          complete(HttpResponse(InternalServerError))
      }
    }

  def handle(
      request: SOAPRequest,
      user: String
  ): Future[ToResponseMarshallable] =
    breaker
      .withCircuitBreaker(conversation.getResponse(user, request))
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeHttpResponse(
              OK,
              MediaTypes.`text/xml`,
              config.mode,
              Some(elm.toString())
            )
          case Left(InvalidRequest) =>
            makeHttpResponse(
              BadRequest,
              MediaTypes.`text/plain`,
              config.mode,
              Some("Invalid request")
            )
          case _ =>
            makeHttpResponse(
              OK,
              MediaTypes.`text/plain`,
              config.mode,
              None
            )
        }
      }
      .recover {
        case _: CircuitBreakerOpenException =>
          Future.successful(
            HttpResponse(TooManyRequests).withEntity("Server Busy")
          )
        case e: Throwable =>
          log.error("Failed " + e.getLocalizedMessage, e)
          Future.successful(HttpResponse(InternalServerError))
      }

  def makeHttpResponse(
      status: StatusCode,
      charset: MediaType.WithOpenCharset,
      mode: String,
      payload: Option[String]
  ) = HttpResponse(
    status = status,
    entity = mode match {
      case "chunked" =>
        payload
          .map(p => {
            HttpEntity.Chunked(
              ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
              Source.single(ChunkStreamPart(p))
            )
          })
          .getOrElse(HttpEntity.Empty)
      case "delimited" =>
        payload
          .map(p => {
            HttpEntity.CloseDelimited(
              ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
              Source.single(ByteString(p))
            )
          })
          .getOrElse(HttpEntity.Empty)
    }
  )

}
