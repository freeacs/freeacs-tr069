package com.github.freeacs.session

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.pattern.pipe
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

object SessionActor {
  def props(user: String, services: Tr069Services)(implicit ec: ExecutionContext): Props =
    Props(new SessionActor(user, services))
}

class SessionActor(user: String, services: Tr069Services)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {

  log.info("Created session actor for " + user)

  private var requests: List[(SOAPRequest, Option[SOAPResponse])] = List()

  override def receive: Receive = {
    case request: SOAPRequest =>
      request match {
        case _: InformRequest =>
          val response = Some(InformResponse())
          requests = List((request, response))
          pipe(Future.successful(response)) to sender
        case EmptyRequest =>
          requests = requests :+ (request, None)
          log.info(requests.map(requestToString).mkString(", "))
          sender ! None
          self ! PoisonPill
      }
    case unknown =>
      log.error(s"Got unknown message $unknown")
      self ! PoisonPill
  }

  private def requestToString(tuple: (SOAPRequest, Option[SOAPResponse])): (String, Option[String]) =
    (tuple._1.getClass.getSimpleName, tuple._2.map(_.getClass.getSimpleName))
}
