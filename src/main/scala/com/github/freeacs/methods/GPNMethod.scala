package com.github.freeacs.methods
import com.github.freeacs.services.UnitService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.History
import com.github.freeacs.session.sessionState.SessionState.HistoryItem.{
  GPNRes,
  GPVReq
}
import com.github.freeacs.session.sessionState.SessionState.State.ExpectGetParameterValuesResponse
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
      services: UnitService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got GPNRes. Returning GPVReq. " + request.toString)
    val response =
      GetParameterValuesRequest(
        Seq("InternetGatewayDevice.ManagementServer.Username")
      )
    Future.successful(
      (
        sessionState.copy(
          state = ExpectGetParameterValuesResponse,
          history = sessionState.history :+ History(GPNRes, GPVReq)
        ),
        response
      )
    )
  }
}
