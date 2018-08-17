package com.github.freeacs.session

import akka.actor.ActorRef
import akka.pattern.ask
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionCache.{
  Cached,
  GetFromCache,
  PutInCache
}
import com.github.freeacs.state.{ExpectInformRequest, FSM}
import com.github.freeacs.state.Transformation._
import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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
            FSM(state.state)
              .transition(request, transformWith(username, services))
              .map(
                result =>
                  (
                    result._1,
                    state.copy(
                      modified = System.currentTimeMillis(),
                      state = result._2.currentState
                    )
                )
              )
              .map { result =>
                cacheActor ! PutInCache(username, result._2)
                result._1
              }
          case _ =>
            val newState = SessionState(
              user = username,
              modified = System.currentTimeMillis(),
              state = ExpectInformRequest
            )
            FSM(newState.state)
              .transition(request, transformWith(username, services))
              .map { result =>
                cacheActor ! PutInCache(
                  username,
                  newState.copy(state = result._2.currentState)
                )
                result._1
              }
        }
    }
  }
}
