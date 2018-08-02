package com.github.freeacs.session

import akka.actor.{Actor, ActorLogging, FSM, PoisonPill, Props}
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._

import scala.concurrent.ExecutionContext

object SessionActor {
  def props(user: String, services: Tr069Services)(implicit ec: ExecutionContext): Props =
    Props(new SessionActor(user, services))
}

trait ConversationState
case object ExpectInform extends ConversationState
case object ExpectEmpty extends ConversationState
case object Complete extends ConversationState
case object Failed extends ConversationState

class SessionActor(user: String, services: Tr069Services)(implicit ec: ExecutionContext)
  extends Actor with FSM[ConversationState, List[(SOAPRequest, Option[SOAPResponse])]] with ActorLogging  {

  log.info("Created session actor for " + user)


  startWith(ExpectInform, List())

  when(ExpectInform) {
    case Event(inform: InformRequest, conversationState) =>
      val newConversationState = conversationState :+ (inform, Some(InformResponse()))
      goto(ExpectEmpty) using(newConversationState) replying(Some(InformResponse()))
    case _ =>
      invalid
  }

  when(ExpectEmpty) {
    case Event(EmptyRequest, conversationState) =>
      val newConversationState = conversationState :+ (EmptyRequest, None)
      goto(Complete) using(newConversationState) replying(None)
    case _ =>
      invalid
  }

  when(Complete) {
    case _ => stay
  }

  when(Failed) {
    case _ => stay
  }

  onTransition {
    case _ -> Failed =>
      log.error("Conversation failure occurred")
      self ! PoisonPill
    case _ -> Complete =>
      log.info(s"Conversation completed ${stateData.map(mapToString).mkString(", ")}")
      self ! PoisonPill
  }

  whenUnhandled {
    case Event(e, s) ⇒
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      self ! PoisonPill
      stay
  }

  def mapToString(tuple: (SOAPRequest, Option[SOAPResponse])): (String, Option[String]) =
    (tuple._1.getClass.getSimpleName, tuple._2.map(_.getClass.getSimpleName))

  def invalid = goto(Failed) replying(Some(InvalidRequest))
}
