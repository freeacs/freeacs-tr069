package com.github.freeacs.routes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import com.github.freeacs.entities._
import com.github.freeacs.marshaller.Marshallers
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionActor
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services)
                 (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext, timeout: Timeout)
  extends Directives with Marshallers {

  val REALM = "FreeACS"

  def routes: Route =
    logRequestResult("tr069") {
      authenticateBasicAsync(REALM, services.authService.authenticator) { user =>
        (post & entity(as[SOAPRequest])) { soapRequest =>
          path("tr069") {
            complete(cb.withCircuitBreaker(handle(soapRequest, user)))
          }
        }
      }
    }

  def handle(soapRequest: SOAPRequest, user: String): Future[ToResponseMarshallable] = {
    val userActor = Await.result(getSessionActor(user), timeout.duration)
    (userActor ? soapRequest).map(_.asInstanceOf[SOAPResponse]).map {
      case informResponse: InformResponse =>
        informResponse
      case EmptyResponse =>
        StatusCodes.OK
    }
  }

  def getSessionActor(user: String): Future[ActorRef] = {
    system.actorSelection(s"user/session-$user")
      .resolveOne()
      .recover { case _: Exception =>
        system.actorOf(SessionActor.props(user, services), s"session-$user")
      }
  }

}
