package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{
  ExpectGetParameterValuesResponse,
  SessionState
}
import com.github.freeacs.xml.{
  GetParameterNamesResponse,
  GetParameterValuesRequest,
  SOAPResponse
}

import scala.concurrent.{ExecutionContext, Future}

object GPNMethod extends AbstractMethod[GetParameterNamesResponse] {
  def process(
      request: GetParameterNamesResponse,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got GPNRes. Returning GPVReq. " + request.toString)
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
