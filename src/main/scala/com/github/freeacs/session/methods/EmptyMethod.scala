package com.github.freeacs.session.methods
import com.github.freeacs.session.{
  ExpectGetParameterNamesResponse,
  SessionState
}
import com.github.freeacs.xml.{
  EmptyRequest,
  GetParameterNamesRequest,
  SOAPResponse
}

import scala.concurrent.Future

object EmptyMethod extends SessionMethod[EmptyRequest] {
  def process(
      request: EmptyRequest,
      sessionState: SessionState
  ): Future[(SessionState, SOAPResponse)] = {
    log.info("Got EM. Returning GPNReq.")
    val response = GetParameterNamesRequest("InternetGatewayDevice.")
    Future.successful(
      (
        sessionState.copy(
          state = ExpectGetParameterNamesResponse,
          history = (sessionState.history :+ ("EM", "GPNReq"))
        ),
        response
      )
    )
  }
}
