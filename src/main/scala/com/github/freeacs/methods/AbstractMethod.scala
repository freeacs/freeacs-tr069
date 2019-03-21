package com.github.freeacs.methods
import com.github.freeacs.services.UnitService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.State.ExpectInformRequest
import com.github.freeacs.xml.{EmptyResponse, SOAPRequest, SOAPResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

trait AbstractMethod[T <: SOAPRequest] {

  val log = LoggerFactory.getLogger(getClass)

  def process(
      request: T,
      state: SessionState,
      services: UnitService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)]

  def resetConversation(
      sessionState: SessionState
  ): (SessionState, SOAPResponse) = {
    (
      SessionState(
        user = sessionState.user,
        modified = System.currentTimeMillis(),
        state = ExpectInformRequest,
        remoteAddress = sessionState.remoteAddress,
        errorCount = 0
      ),
      EmptyResponse()
    )
  }
}
