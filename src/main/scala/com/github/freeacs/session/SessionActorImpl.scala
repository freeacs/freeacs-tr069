package com.github.freeacs.session

import akka.actor.TypedProps
import com.github.freeacs.entities._
import com.github.freeacs.services.Tr069Services

import scala.concurrent.Future

class SessionActorImpl(user: String, services: Tr069Services) extends SessionActor {

  var requests = Seq[SOAPRequest]()

  def request(request: SOAPRequest): Future[SOAPResponse] = request match {
    case inf: InformRequest =>
      requests = Seq(inf) // reset
      Future.successful(InformResponse())
    case EmptyRequest =>
      requests = requests :+ EmptyRequest
      Future.successful(EmptyResponse)
  }
}

object SessionActorImpl {
  def props(user: String, services: Tr069Services): TypedProps[SessionActor] =
    TypedProps(classOf[SessionActor], new SessionActorImpl(user, services))
}
