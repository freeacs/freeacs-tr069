package com.github.freeacs.methods
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.History
import com.github.freeacs.session.sessionState.SessionState.HistoryItem.{
  EM,
  SPVRes
}
import com.github.freeacs.xml.{SOAPResponse, SetParameterValuesResponse}

import scala.concurrent.{ExecutionContext, Future}

object SPVMethod extends AbstractMethod[SetParameterValuesResponse] {
  def process(
      request: SetParameterValuesResponse,
      sessionState: SessionState,
      services: DaoService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got SPVRes. Returning EM. " + request.toString)
    val history = sessionState.history :+ History(SPVRes, EM)
    log.info("Event: " + history.mkString(", "))
    Future.successful(resetConversation(sessionState))
  }
}
