package com.github.freeacs.actors

import akka.actor.{Actor, ActorLogging, FSM, PoisonPill, Props}
import akka.pattern.pipe
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

object ConversationActor {
  def props(user: String, services: Tr069Services)(implicit ec: ExecutionContext): Props =
    Props(new ConversationActor(user, services))
}

class ConversationActor(user: String, services: Tr069Services)(implicit ec: ExecutionContext)
  extends Actor with FSM[ConversationState, ConversationData] with ActorLogging {

  log.info("Created session actor for " + user)

  startWith(ExpectInformRequest, ConversationData())

  when(ExpectInformRequest) {
    case Event(request: InformRequest, stateData) =>
      val response = InformResponse()
      val newConversationState = stateData.copy(history = stateData.history :+ (request, response))
      services.getUnit(user)
        .flatMap {
          case Some(unit) =>
            Future.successful(newConversationState.copy(unit = Some(unit)))
          case _ =>
            // TODO check if unit type exists
            // if not create it
            // create new unit
           Future.successful(newConversationState) // FIXME replace
        }.recover {
        case e =>
          log.error(s"Failed to retrieve unit params {}", e)
          newConversationState.copy(exception = Some(e))
      }.map(state =>
        if (state.exception.isEmpty)
          GoTo(ExpectEmptyRequest, state)
        else
          GoTo(Failed, state)
      ) pipeTo self
      goto(ExpectGoTo) replying (response)
    case Event(request, stateData) =>
      log.error("Expecting inform but got {}. Data: {}", request, stateData)
      goto(Failed) replying (InvalidRequest)
  }

  when(ExpectGoTo) {
    case Event(GoTo(state, stateData), _) =>
      goto(state) using (stateData)
  }

  when(ExpectEmptyRequest) {
    case Event(EmptyRequest, stateData) =>
      val response = GetParameterNamesRequest(Seq("InternetGatewayDevice."))
      val newConversationState = stateData.copy(history = stateData.history :+ (EmptyRequest, response))
      goto(ExpectGetParameterNamesResponse) using (newConversationState) replying (response)
  }

  when(ExpectGetParameterNamesResponse) {
    case Event(res: GetParameterNamesResponse, stateData) =>
      val newConversationState = stateData.copy(history = stateData.history :+ (res, EmptyResponse))
      goto(Complete) using(newConversationState) replying(EmptyResponse)
  }

  when(Complete) {
    case _ => stay
  }

  when(Failed) {
    case _ => stay
  }

  onTransition {
    case state -> Failed =>
      log.error("Conversation failure occurred from state {} with data {}", state, nextStateData)
      self ! PoisonPill
    case state -> Complete =>
      log.info(s"Conversation completed from state {} with data {}", state, nextStateData)
      self ! PoisonPill
  }

  whenUnhandled {
    case Event(e, s) ⇒
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      self ! PoisonPill
      stay
  }

  initialize()
}
