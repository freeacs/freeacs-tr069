package com.github.freeacs.state
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionState
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

final case class TransformationResult(
    response: SOAPResponse,
    state: SessionState
)

final case class Transformation(
    fn: (SessionState, SOAPRequest) => Future[TransformationResult]
) {
  def apply(
      currentState: SessionState,
      request: SOAPRequest
  ): Future[TransformationResult] =
    fn(currentState, request)
}

object Transformation {
  val log = LoggerFactory.getLogger(getClass)

  def transformWith(user: String, services: Tr069Services) =
    Transformation {
      case (
          sessionState @ SessionState(_, _, ExpectInformRequest, _),
          r: InformRequest
          ) =>
        log.info("Got INReq. Returning INRes. " + r.toString)
        Future.successful(
          TransformationResult(
            InformResponse(),
            sessionState.copy(
              state = ExpectEmptyRequest,
              modified = System.currentTimeMillis()
            )
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
            sessionState.copy(
              state = ExpectGetParameterNamesResponse,
              modified = System.currentTimeMillis()
            )
          )
        )

      case (
          sessionState @ SessionState(_, _, ExpectGetParameterNamesResponse, _),
          r: GetParameterNamesResponse
          ) =>
        log.info("Got GPNRes. Returning GPVReq. " + r.toString)
        val response =
          GetParameterValuesRequest(
            Seq(("InternetGatewayDevice.ManagementServer.Username"))
          )
        Future.successful(
          TransformationResult(
            response,
            sessionState.copy(
              state = ExpectGetParameterValuesResponse,
              modified = System.currentTimeMillis()
            )
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
            sessionState.copy(
              state = ExpectSetParameterValuesResponse,
              modified = System.currentTimeMillis()
            )
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
            sessionState.copy(
              state = ExpectInformRequest,
              modified = System.currentTimeMillis()
            )
          )
        )

      case (sessionState, req) =>
        log.error(s"Got unexpected request $req in state ${sessionState.state}")
        Future.successful(
          TransformationResult(
            InvalidRequest,
            if (sessionState.errorCount < 2)
              sessionState.copy(
                errorCount = (sessionState.errorCount + 1),
                modified = System.currentTimeMillis()
              )
            else
              sessionState.copy(
                errorCount = 0,
                state = ExpectInformRequest,
                modified = System.currentTimeMillis()
              )
          )
        )
    }

}
