package com.github.freeacs.methods
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.{EM, SPVRes, SessionState}
import com.github.freeacs.xml.{SOAPResponse, SetParameterValuesResponse}

import scala.concurrent.{ExecutionContext, Future}

object SPVMethod extends AbstractMethod[SetParameterValuesResponse] {
  def process(
      request: SetParameterValuesResponse,
      sessionState: SessionState,
      services: DaoService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got SPVRes. Returning EM. " + request.toString)
    val history = sessionState.history :+ (SPVRes, EM)
    log.info("Event: " + history.mkString(", "))
    Future.successful(resetConversation(sessionState))
  }
}
