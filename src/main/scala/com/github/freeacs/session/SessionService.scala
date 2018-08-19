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
import com.github.jarlah.authenticscala.AuthenticationContext

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
      request: SOAPRequest,
      context: AuthenticationContext
  ): Future[SOAPResponse] = {
    (cacheActor ? GetFromCache(username)).flatMap {
      case Cached(_: String, maybeState: Option[SessionState]) =>
        maybeState match {
          case Some(state) =>
            state.transition(services, request).map { result =>
              cacheActor ! PutInCache(
                username,
                result._1.copy(modified = System.currentTimeMillis())
              )
              result._2
            }
          case _ =>
            SessionState(
              user = username,
              remoteAddress = context.remoteAddress,
              modified = System.currentTimeMillis(),
              state = ExpectInformRequest,
              history = List.empty
            ).transition(services, request).map { result =>
              cacheActor ! PutInCache(username, result._1)
              result._2
            }
        }
    }
  }
}
