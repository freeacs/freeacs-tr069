package com.github.freeacs.session

import akka.actor.ActorRef
import com.github.freeacs.session.SessionCache.{
  Cached,
  GetFromCache,
  PutInCache
}
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.Tr069Services
import akka.pattern.ask
import com.github.freeacs.state.{ExpectInformRequest, FSM}
import com.github.freeacs.state.Transformation._
import com.github.freeacs.xml.{EmptyResponse, SOAPRequest, SOAPResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class SessionService(
    services: Tr069Services,
    config: Configuration,
    cacheActor: ActorRef
)(implicit ec: ExecutionContext) {

  implicit def akkaTimeout = akka.util.Timeout(300.milliseconds)

  def getResponse(
      username: String,
      request: SOAPRequest
  ): Future[SOAPResponse] = {
    (cacheActor ? GetFromCache(username)).flatMap {
      case Cached(_: String, maybeState: Option[SessionState]) =>
        maybeState match {
          case Some(state) =>
            state.fsm
              .transition(request, transformer(username, services))
              .map(
                fsm =>
                  state.copy(
                    lastModified = System.currentTimeMillis(),
                    fsm = fsm
                )
              )
              .map { state =>
                cacheActor ! PutInCache(username, state)
                state.fsm.currentState.response
              }
          case _ =>
            var newState = SessionState(
              username = username,
              lastModified = System.currentTimeMillis(),
              fsm = FSM(ExpectInformRequest(EmptyResponse))
            )
            newState.fsm
              .transition(request, transformer(username, services))
              .map { fsm =>
                cacheActor ! PutInCache(username, newState.copy(fsm = fsm))
                fsm.currentState.response
              }
        }
    }
  }
}
