package com.github.freeacs.session

import akka.actor.{Actor, Props}
import com.github.freeacs.entities._
import com.github.freeacs.services.Tr069Services

class SessionActor(user: String, services: Tr069Services) extends Actor {

  override def receive: Receive = {
    case _: InformRequest =>
      sender ! InformResponse()
    case _: UnknownRequest =>
      sender ! EmptyResponse
  }
}

object SessionActor {
  def props(user: String, services: Tr069Services): Props =
    Props(new SessionActor(user, services))
}
