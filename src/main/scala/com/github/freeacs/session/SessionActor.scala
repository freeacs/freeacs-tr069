package com.github.freeacs.session

import akka.actor.{Actor, ActorLogging, Props}
import com.github.freeacs.entities._
import com.github.freeacs.services.Tr069Services

class SessionActor(user: String, services: Tr069Services) extends Actor with ActorLogging {

  log.info(s"Created session actor for $user")

  var requests = Seq[SOAPRequest]()

  override def receive: Receive = {
    case inf: InformRequest =>
      requests = requests :+ inf
      sender ! InformResponse()
    case EmptyRequest =>
      requests = requests :+ EmptyRequest
      sender ! EmptyResponse
      log.info(requests.map(_.getClass.getSimpleName).mkString(", "))
      context.stop(self)
  }
}

object SessionActor {
  def props(user: String, services: Tr069Services): Props =
    Props(new SessionActor(user, services))
}
