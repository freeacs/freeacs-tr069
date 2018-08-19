package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionState
import com.github.freeacs.xml.{SOAPResponse, SetParameterValuesResponse}

import scala.concurrent.{ExecutionContext, Future}

object SPVMethod extends AbstractMethod[SetParameterValuesResponse] {
  def process(
      request: SetParameterValuesResponse,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got SPVRes. Returning EM. " + request.toString)
    val history = sessionState.history :+ ("SPVRes", "EM")
    log.info("Event: " + history.mkString(", "))
    Future.successful(resetConversation(sessionState))
  }
}
