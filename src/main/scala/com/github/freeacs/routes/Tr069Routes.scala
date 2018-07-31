package com.github.freeacs.routes

import akka.actor.{ActorSystem, TypedActor}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import akka.util.Timeout
import com.github.freeacs.entities._
import com.github.freeacs.marshaller.Marshallers
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{SessionActor, SessionActorImpl}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives with Marshallers {

  implicit val timeout = Timeout(5.seconds)

  val REALM = "FreeACS"

  def routes: Route =
    logRequestResult("tr069") {
      authenticateBasicAsync(REALM, services.authService.authenticator) { user =>
        (post & entity(as[SOAPRequest])) { soapRequest =>
          path("tr069") {
            handle(soapRequest, user)
          }
        }
      }
    }

  def handle(soapRequest: SOAPRequest, user: String) = {
    val withBreaker = getSessionActor(user).flatMap(userActor =>
        cb.withCircuitBreaker(userActor.request(soapRequest)))
    onComplete(withBreaker) {
      case Success(inform: InformResponse) =>
        complete(inform)
      case Success(EmptyResponse) =>
        complete(StatusCodes.OK)
      case Failure(_: CircuitBreakerOpenException) =>
        complete(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case Failure(_) =>
        complete(StatusCodes.InternalServerError)
    }
  }

  def getSessionActor(user: String): Future[SessionActor] = {
    system.actorSelection(s"user/session-$user")
      .resolveOne()
      .map(actorRef => TypedActor(system).typedActorOf(SessionActorImpl.props(user, services), actorRef))
      .recover { case _: Exception =>
        TypedActor(system).typedActorOf(SessionActorImpl.props(user, services), s"session-$user")
      }
  }

}
