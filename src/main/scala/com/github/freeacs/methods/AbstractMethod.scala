package com.github.freeacs.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{ExpectInformRequest, SessionState}
import com.github.freeacs.xml.{EmptyResponse, SOAPRequest, SOAPResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

trait AbstractMethod[T <: SOAPRequest] {

  val log = LoggerFactory.getLogger(getClass)

  def process(
      request: T,
      state: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)]

  protected[this] def resetConversation(
      sessionState: SessionState
  ): (SessionState, SOAPResponse) = {
    (
      SessionState(
        user = sessionState.user,
        modified = System.currentTimeMillis(),
        state = ExpectInformRequest,
        remoteAddress = sessionState.remoteAddress
      ),
      EmptyResponse()
    )
  }
}
