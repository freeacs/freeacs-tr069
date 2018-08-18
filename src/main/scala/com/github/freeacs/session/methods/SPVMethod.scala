package com.github.freeacs.session.methods
import com.github.freeacs.session.{ExpectInformRequest, SessionState}
import com.github.freeacs.xml.{
  EmptyResponse,
  SOAPResponse,
  SetParameterValuesResponse
}

import scala.concurrent.Future

object SPVMethod extends SessionMethod[SetParameterValuesResponse] {
  def process(
      request: SetParameterValuesResponse,
      sessionState: SessionState
  ): Future[(SessionState, SOAPResponse)] = {
    log.info("Got SPVRes. Returning EM. " + request.toString)
    val history = sessionState.history :+ ("SPVRes", "EM")
    log.info("Event: " + history.mkString(", "))
    val response = EmptyResponse()
    Future.successful(
      (
        sessionState.copy(
          state = ExpectInformRequest,
          history = List.empty
        ),
        response
      )
    )
  }
}
