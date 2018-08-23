package com.github.freeacs.methods
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.{
  ExpectGetParameterValuesResponse,
  GPNRes,
  GPVReq,
  SessionStateTransformer
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
      services: DaoService
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
          history = (sessionState.history :+ (GPNRes, GPVReq))
        ),
        response
      )
    )
  }
}
