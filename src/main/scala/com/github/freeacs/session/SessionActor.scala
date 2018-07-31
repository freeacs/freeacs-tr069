package com.github.freeacs.session

import akka.actor.TypedProps
import com.github.freeacs.entities.{SOAPRequest, SOAPResponse}
import com.github.freeacs.services.Tr069Services

import scala.concurrent.Future

trait SessionActor {
  def request(request: SOAPRequest): Future[Option[SOAPResponse]]
}

object SessionActor {
  def props(user: String, services: Tr069Services): TypedProps[SessionActor] =
    TypedProps(classOf[SessionActor], new SessionActorImpl(user, services))
}