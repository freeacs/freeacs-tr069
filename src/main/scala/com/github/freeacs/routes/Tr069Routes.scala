package com.github.freeacs.routes

import akka.actor.{ActorSystem, TypedActor}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException}
import akka.stream.Materializer
import com.github.freeacs.entities._
import com.github.freeacs.marshaller.Marshallers
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionActor

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services, sessionLookupTimeout: FiniteDuration)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives with Marshallers {

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

  def handle(soapRequest: SOAPRequest, user: String): Route = {
    val withBreaker = cb.withCircuitBreaker(
      getSessionActor(user).flatMap(userActor =>
        userActor.request(soapRequest)))
    onComplete(withBreaker) {
      case Success(Some(inform: InformResponse)) =>
        complete(inform)
      case Success(None) =>
        complete(StatusCodes.OK)
      case Failure(_: CircuitBreakerOpenException) =>
        complete(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case Failure(_) =>
        complete(StatusCodes.InternalServerError)
    }
  }

  def getSessionActor(user: String): Future[SessionActor] = {
    val actorName = s"session-$user"
    val actorProps = SessionActor.props(user, services)
    system.actorSelection(s"user/$actorName")
      .resolveOne(sessionLookupTimeout)
      .map(actorRef => TypedActor(system).typedActorOf(actorProps, actorRef))
      .recover { case _: Exception =>
        TypedActor(system).typedActorOf(actorProps, actorName)
      }
  }

}
