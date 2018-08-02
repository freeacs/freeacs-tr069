package com.github.freeacs.session

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.pipe
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object SessionActor {
  def props(user: String, services: Tr069Services)(implicit ec: ExecutionContext): Props =
    Props(new SessionActor(user, services))
}

class SessionActor(user: String, services: Tr069Services)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {

  log.info("Created session actor for " + user)

  private var requests: mutable.Map[SOAPRequest, Option[SOAPResponse]] = mutable.LinkedHashMap()

  override def receive: Receive = {
    case request: SOAPRequest =>
      request match {
        case inform: InformRequest =>
          val response = Some(InformResponse())
          requests = mutable.Map()
          requests.put(inform, response)
          pipe(Future.successful(response)) to sender
        case EmptyRequest =>
          requests.put(EmptyRequest, None)
          pipe(Future.successful(None)) to sender
          self ! PoisonPill
      }
    case unknown =>
      log.error(s"Got unknown message $unknown")
      sender ! PoisonPill
  }

}
