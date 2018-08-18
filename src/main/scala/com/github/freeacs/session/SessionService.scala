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
            state.transition(username, services, request).map { result =>
              cacheActor ! PutInCache(
                username,
                result.state.copy(modified = System.currentTimeMillis())
              )
              result.response
            }
          case _ =>
            val newState = SessionState(
              user = username,
              modified = System.currentTimeMillis(),
              state = ExpectInformRequest
            )
            newState.transition(username, services, request).map { result =>
              cacheActor ! PutInCache(
                username,
                result.state // modified has been set above
              )
              result.response
            }
        }
    }
  }
}
