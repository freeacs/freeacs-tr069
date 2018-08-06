package com.github.freeacs.routes

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.actors.ConversationActor
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Tr069Routes(breaker: CircuitBreaker, services: Tr069Services, authService: AuthenticationService, timeout: FiniteDuration)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives {

  def routes: Route =
    logRequestResult("tr069") {
      authenticateBasicAsync("FreeACS", authService.authenticator) { user =>
        (post & entity(as[SOAPRequest])) { soapRequest =>
          path("tr069") {
            handle(soapRequest, user)
          }
        }
      }
    }

  def handle(soapRequest: SOAPRequest, user: String): Route = {
    implicit val requestTimeout: Timeout = timeout
    val withBreaker = breaker.withCircuitBreaker(
      getConversationActor(user).flatMap(_ ? soapRequest)
    ).map(_.asInstanceOf[SOAPResponse])
    onComplete(withBreaker) {
      case Success(response: SOAPResponse) =>
        complete(
          Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
            case Right(elm) =>
              makeHttpResponse(StatusCodes.OK, MediaTypes.`text/xml`, Some(elm.toString()))
            case Left(InvalidRequest) =>
              makeHttpResponse(StatusCodes.BadRequest, MediaTypes.`text/plain`, Some("Invalid request"))
            case _ =>
              makeHttpResponse(StatusCodes.OK, MediaTypes.`text/plain`, None)
          }
        )
      case Failure(_: CircuitBreakerOpenException) =>
        complete(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case Failure(e) =>
        e.printStackTrace()
        complete(StatusCodes.InternalServerError)
    }
  }

  private def makeHttpResponse(status: StatusCode, charset: MediaType.WithOpenCharset, str: Option[String]) = {
    HttpResponse(
      status = status,
      entity = HttpEntity.CloseDelimited(
        ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
        Source.single(str.map(ByteString.apply).getOrElse(ByteString.empty))
      )
    )
  }

  def getConversationActor(user: String): Future[ActorRef] = {
    val actorName = s"conversation-$user"
    val actorProps = ConversationActor.props(user, services)
    system.actorSelection(s"user/$actorName")
      .resolveOne(timeout)
      .recover { case _: Exception =>
        system.actorOf(actorProps, actorName)
      }
  }

}
