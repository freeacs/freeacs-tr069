package com.github.freeacs.session.methods
import com.github.freeacs.session.{
  ExpectSetParameterValuesResponse,
  SessionState
}
import com.github.freeacs.xml.{
  GetParameterValuesResponse,
  SOAPResponse,
  SetParameterValuesRequest
}

import scala.concurrent.Future

object GPVMethod extends SessionMethod[GetParameterValuesResponse] {
  def process(
      request: GetParameterValuesResponse,
      sessionState: SessionState
  ): Future[(SessionState, SOAPResponse)] = {
    log.info("Got GPVRes. Returning SPVReq. " + request.toString)
    val response = SetParameterValuesRequest()
    Future.successful(
      (
        sessionState.copy(
          state = ExpectSetParameterValuesResponse,
          history = (sessionState.history :+ ("GPVRes", "SPVReq"))
        ),
        response
      )
    )
  }
}
