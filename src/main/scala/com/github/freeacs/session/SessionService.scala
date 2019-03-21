package com.github.freeacs.session

import akka.actor.ActorRef
import akka.pattern.ask
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.UnitService
import com.github.freeacs.session.SessionCache.{
  Cached,
  GetFromCache,
  PutInCache
}
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.State.ExpectInformRequest
import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}
import com.github.jarlah.authenticscala.AuthenticationContext

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SessionService(
    unitService: UnitService,
    config: Configuration,
    cacheActor: ActorRef
)(implicit ec: ExecutionContext) {

  implicit def akkaTimeout = akka.util.Timeout(300.milliseconds)

  def getResponse(
      username: String,
      request: SOAPRequest,
      context: AuthenticationContext
  ): Future[SOAPResponse] = {
    (cacheActor ? GetFromCache(username)).flatMap {
      case Cached(_: String, maybeState: Option[Any]) =>
        maybeState match {
          case Some(state) =>
            SessionStateTransformer
              .transition(
                state.asInstanceOf[SessionState],
                unitService,
                request
              )
              .map { result =>
                cacheActor ! PutInCache(
                  username,
                  result._1.copy(modified = System.currentTimeMillis())
                )
                result._2
              }
          case _ =>
            SessionStateTransformer
              .transition(
                SessionState(
                  user = username,
                  modified = System.currentTimeMillis(),
                  state = ExpectInformRequest,
                  remoteAddress = context.remoteAddress,
                  errorCount = 0
                ),
                unitService,
                request
              )
              .map { result =>
                cacheActor ! PutInCache(username, result._1)
                result._2
              }
        }
    }
  }
}
