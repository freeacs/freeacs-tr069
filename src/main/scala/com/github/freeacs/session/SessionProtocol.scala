package com.github.freeacs.session
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object SessionProtocol {
  val log = LoggerFactory.getLogger(getClass)

  def protocol(user: String, services: Tr069Services) =
    Transformation {
      case (
          sessionState @ SessionState(_, _, ExpectInformRequest, _),
          request: InformRequest
          ) =>
        log.info("Got INReq. Returning INRes. " + request.toString)
        Future.successful(
          TransformationResult(
            InformResponse(),
            sessionState.copy(state = ExpectEmptyRequest)
          )
        )

      case (
          sessionState @ SessionState(_, _, ExpectEmptyRequest, _),
          EmptyRequest
          ) =>
        log.info("Got EM. Returning GPNReq.")
        val response = GetParameterNamesRequest("InternetGatewayDevice.")
        Future.successful(
          TransformationResult(
            response,
            sessionState.copy(state = ExpectGetParameterNamesResponse)
          )
        )

      case (
          sessionState @ SessionState(_, _, ExpectGetParameterNamesResponse, _),
          request: GetParameterNamesResponse
          ) =>
        log.info("Got GPNRes. Returning GPVReq. " + request.toString)
        val response =
          GetParameterValuesRequest(
            Seq(("InternetGatewayDevice.ManagementServer.Username"))
          )
        Future.successful(
          TransformationResult(
            response,
            sessionState.copy(state = ExpectGetParameterValuesResponse)
          )
        )

      case (
          sessionState @ SessionState(
            _,
            _,
            ExpectGetParameterValuesResponse,
            _
          ),
          request: GetParameterValuesResponse
          ) =>
        log.info("Got GPVRes. Returning SPVReq. " + request.toString)
        val response = SetParameterValuesRequest()
        Future.successful(
          TransformationResult(
            response,
            sessionState.copy(state = ExpectSetParameterValuesResponse)
          )
        )

      case (
          sessionState @ SessionState(
            _,
            _,
            ExpectSetParameterValuesResponse,
            _
          ),
          request: SetParameterValuesResponse
          ) =>
        log.info("Got SPVRes. Returning EM. " + request.toString)
        val response = EmptyResponse
        Future.successful(
          TransformationResult(
            response,
            sessionState.copy(state = ExpectInformRequest)
          )
        )

      case (sessionState, req) =>
        log.error(s"Got unexpected request $req in state ${sessionState.state}")
        Future.successful(
          TransformationResult(
            InvalidRequest,
            if (sessionState.errorCount < 2)
              sessionState.copy(errorCount = (sessionState.errorCount + 1))
            else
              sessionState.copy(errorCount = 0, state = ExpectInformRequest)
          )
        )
    }

  final case class TransformationResult(
      response: SOAPResponse,
      state: SessionState
  )

  final case class Transformation(
      fn: (SessionState, SOAPRequest) => Future[TransformationResult]
  ) {
    def transform(
        currentState: SessionState,
        request: SOAPRequest
    ): Future[TransformationResult] =
      fn(currentState, request)
  }
}
