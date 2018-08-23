package com.github.freeacs.methods
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.{
  ExpectSetParameterValuesResponse,
  GPVRes,
  SPVReq,
  SessionStateTransformer
}
import com.github.freeacs.xml.{
  GetParameterValuesResponse,
  SOAPResponse,
  SetParameterValuesRequest
}

import scala.concurrent.{ExecutionContext, Future}

object GPVMethod extends AbstractMethod[GetParameterValuesResponse] {
  def process(
      request: GetParameterValuesResponse,
      sessionState: SessionState,
      services: DaoService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got GPVRes. Returning SPVReq. " + request.toString)
    val response = SetParameterValuesRequest()
    Future.successful(
      (
        sessionState.copy(
          state = ExpectSetParameterValuesResponse,
          history = (sessionState.history :+ (GPVRes, SPVReq))
        ),
        response
      )
    )
  }
}
