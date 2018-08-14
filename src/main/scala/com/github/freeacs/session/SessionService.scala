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
    (cacheActor ? GetFromCache(username)).map {
      case Cached(_: String, maybeState: Option[SessionState]) =>
        maybeState
          .map(
            state =>
              state.copy(
                lastModified = System.currentTimeMillis(),
                fsm =
                  state.fsm.transition(request, transformer(username, services))
            )
          )
          .map { state =>
            cacheActor ! PutInCache(username, state)
            state.fsm.currentState.response
          }
          .getOrElse {
            val newState =
              SessionState(
                username = username,
                lastModified = System.currentTimeMillis(),
                fsm = FSM(ExpectInformRequest(EmptyResponse))
                  .transition(request, transformer(username, services))
              )
            cacheActor ! PutInCache(username, newState)
            newState.fsm.currentState.response
          }
    }
  }
}
