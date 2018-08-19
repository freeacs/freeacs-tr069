package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthService, Tr069Services}
import com.github.freeacs.session.SessionService
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._
import com.github.jarlah.authenticscala.AuthenticationContext
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.xml.NodeSeq

class Routes(
    breaker: CircuitBreaker,
    services: Tr069Services,
    authService: AuthService,
    config: Configuration,
    conversation: SessionService
)(implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
    extends Directives
    with Auth {

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
            authenticateConversation(authService.getSecret, config.authMethod) {
              case (user, context) =>
                entity(as[SOAPRequest]) { soapRequest =>
                  complete(handle(soapRequest, user, context))
                }
            }
          }
        }
      }

  def handle(
      request: SOAPRequest,
      user: String,
      context: AuthenticationContext
  ): Future[ToResponseMarshallable] =
    breaker
      .withCircuitBreaker(conversation.getResponse(user, request, context))
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeHttpResponse(
              OK,
              MediaTypes.`text/xml`,
              config.mode,
              Some(elm.toString())
            )
          case Left(InvalidRequest()) =>
            makeHttpResponse(
              BadRequest,
              MediaTypes.`text/plain`,
              config.mode,
              Some("Invalid request")
            )
          case _ =>
            makeHttpResponse(
              NoContent,
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
    headers = payload
      .filter(_ => charset == MediaTypes.`text/xml`)
      .map(_ => immutable.Seq(RawHeader("SOAPAction", "")))
      .getOrElse(immutable.Seq.empty),
    entity = payload.map { p =>
      mode match {
        case "chunked" =>
          HttpEntity.Chunked(
            ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
            Source.single(ChunkStreamPart(p))
          )
        case "delimited" =>
          HttpEntity.CloseDelimited(
            ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
            Source.single(ByteString(p))
          )
      }
    }.getOrElse(HttpEntity.Empty)
  )

}
