package com.github.freeacs.session

import com.github.freeacs.methods._
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.State.{
  ExpectEmptyRequest,
  ExpectGetParameterNamesResponse,
  ExpectGetParameterValuesResponse,
  ExpectInformRequest,
  ExpectSetParameterValuesResponse
}
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

object SessionStateTransformer {

  def transition(
      state: SessionState,
      services: DaoService,
      request: SOAPRequest
  )(
      implicit ec: ExecutionContext
  ): Future[(SessionState, SOAPResponse)] = request match {
    case request: InformRequest if state.state == ExpectInformRequest =>
      INMethod.process(request, state, services)

    case request: EmptyRequest if state.state == ExpectEmptyRequest =>
      EMMethod.process(request, state, services)

    case request: GetParameterNamesResponse
        if state.state == ExpectGetParameterNamesResponse =>
      GPNMethod.process(request, state, services)

    case request: GetParameterValuesResponse
        if state.state == ExpectGetParameterValuesResponse =>
      GPVMethod.process(request, state, services)

    case request: SetParameterValuesResponse
        if state.state == ExpectSetParameterValuesResponse =>
      SPVMethod.process(request, state, services)

    case _ =>
      val newState =
        if (state.errorCount < 2)
          state.copy(errorCount = state.errorCount + 1)
        else
          state.copy(
            errorCount = 0,
            state = ExpectInformRequest,
            history = List.empty
          )
      Future.successful((newState, InvalidRequest()))
  }
}
