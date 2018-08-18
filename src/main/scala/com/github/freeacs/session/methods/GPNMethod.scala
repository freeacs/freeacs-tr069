package com.github.freeacs.session.methods
import com.github.freeacs.session.{
  ExpectGetParameterValuesResponse,
  SessionState
}
import com.github.freeacs.xml.{
  GetParameterNamesResponse,
  GetParameterValuesRequest,
  SOAPResponse
}

import scala.concurrent.Future

object GPNMethod extends SessionMethod[GetParameterNamesResponse] {
  def process(
      request: GetParameterNamesResponse,
      sessionState: SessionState
  ): Future[(SessionState, SOAPResponse)] = {
    log.info("Got GPNRes. Returnirequestng GPVReq. " + request.toString)
    val response =
      GetParameterValuesRequest(
        Seq(("InternetGatewayDevice.ManagementServer.Username"))
      )
    Future.successful(
      (
        sessionState.copy(
          state = ExpectGetParameterValuesResponse,
          history = (sessionState.history :+ ("GPNRes", "GPVReq"))
        ),
        response
      )
    )
  }
}
