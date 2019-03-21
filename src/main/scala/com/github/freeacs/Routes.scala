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
import com.github.freeacs.services.UnitService
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
    service: UnitService,
    config: Configuration,
    conversation: SessionService
)(implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
    extends Directives
    with Auth {
  private val log = LoggerFactory.getLogger(getClass)

  val passwordRetriever    = service.getUnitSecret
  val authenticationMethod = config.authMethod

  def routes: Route =
    get {
      path("health") {
        complete(s"${config.name} OK")
      }
    } ~
      post {
        path("tr069") {
          logRequestResult("tr069") {
            authenticateConversation {
              case (user, context) =>
                entity(as[SOAPRequest]) { soapRequest =>
                  complete(handle(soapRequest, user, context))
                }
            }
          }
        }
      }

  private def handle(
      request: SOAPRequest,
      user: String,
      context: AuthenticationContext
  ): Future[ToResponseMarshallable] = {
    breaker
      .withCircuitBreaker(conversation.getResponse(user, request, context))
      .map[ToResponseMarshallable] { response =>
        Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
          case Right(elm) =>
            makeResponse(
              status = OK,
              payload = Some(elm.toString()),
              charset = MediaTypes.`text/xml`
            )
          case Left(InvalidRequest()) =>
            makeResponse(
              status = BadRequest,
              payload = Some("Invalid request")
            )
          case _ =>
            makeResponse(
              status = NoContent
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
  }

  private def makeResponse(
      status: StatusCode,
      payload: Option[String] = None,
      charset: MediaType.WithOpenCharset = MediaTypes.`text/plain`
  ) = HttpResponse(
    status = status,
    headers = payload
      .filter(_ => charset == MediaTypes.`text/xml`)
      .map(_ => immutable.Seq(RawHeader("SOAPAction", "")))
      .getOrElse(immutable.Seq.empty),
    entity = payload.map { p =>
      config.mode match {
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
