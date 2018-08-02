package com.github.freeacs.routes

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionActor
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services, sessionLookupTimeout: FiniteDuration)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives with Marshallers {

  def routes: Route =
    logRequestResult("tr069") {
      authenticateBasicAsync("FreeACS", services.authService.authenticator) { user =>
        (post & entity(as[SOAPRequest])) { soapRequest =>
          path("tr069") {
            handle(soapRequest, user)
          }
        }
      }
    }

  def handle(soapRequest: SOAPRequest, user: String): Route = {
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)
    val withBreaker = cb.withCircuitBreaker(getSessionActor(user)
      .flatMap(_ ? soapRequest))
      .map(_.asInstanceOf[Option[SOAPResponse]])
    onComplete(withBreaker) {
      case Success(Some(inform: InformResponse)) =>
        complete(inform)
      case Success(Some(InvalidRequest)) =>
        complete(HttpResponse(StatusCodes.BadRequest).withEntity("Invalid request"))
      case Success(None) =>
        complete(StatusCodes.OK)
      case Failure(_: CircuitBreakerOpenException) =>
        complete(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case Failure(e) =>
        e.printStackTrace()
        complete(StatusCodes.InternalServerError)
    }
  }

  def getSessionActor(user: String): Future[ActorRef] = {
    val actorName = s"session-$user"
    val actorProps = SessionActor.props(user, services)
    system.actorSelection(s"user/$actorName")
      .resolveOne(sessionLookupTimeout)
      .recover { case _: Exception =>
        system.actorOf(actorProps, actorName)
      }
  }

}
