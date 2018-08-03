package com.github.freeacs.routes

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.stream.Materializer
import akka.util.Timeout
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.session.SessionActor
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Tr069Routes(breaker: CircuitBreaker, services: Tr069Services, authService: AuthenticationService, sessionLookupTimeout: FiniteDuration)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives with Marshallers {

  implicit val requestTimeout: Timeout = Timeout(1, TimeUnit.SECONDS)

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
    val withBreaker = breaker.withCircuitBreaker(getSessionActor(user)
      .flatMap(_ ? soapRequest))
      .map(_.asInstanceOf[SOAPResponse])
    onComplete(withBreaker) {
      case Success(inform: InformResponse) =>
        complete(inform)
      case Success(InvalidRequest) =>
        complete(HttpResponse(StatusCodes.BadRequest).withEntity("Invalid request"))
      case Success(EmptyResponse) =>
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
