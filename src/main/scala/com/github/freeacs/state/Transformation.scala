package com.github.freeacs.state
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

final case class TransformationResult(response: SOAPResponse, state: State)

final case class Transformation(
    fn: (State, SOAPRequest) => Future[TransformationResult]
) {
  def apply(
      currentState: State,
      request: SOAPRequest
  ): Future[TransformationResult] =
    fn(currentState, request)
}

object Transformation {
  val log = LoggerFactory.getLogger(getClass)

  def transformWith(user: String, services: Tr069Services) =
    Transformation {
      case (ExpectInformRequest, r: InformRequest) =>
        log.info("Got INReq. Returning INRes. " + r.toString)
        Future.successful(
          TransformationResult(InformResponse(), ExpectEmptyRequest)
        )

      case (ExpectEmptyRequest, EmptyRequest) =>
        log.info("Got EM. Returning GPNReq.")
        val response = GetParameterNamesRequest("InternetGatewayDevice.")
        Future.successful(
          TransformationResult(response, ExpectGetParameterNamesResponse)
        )

      case (ExpectGetParameterNamesResponse, r: GetParameterNamesResponse) =>
        log.info("Got GPNRes. Returning GPVReq. " + r.toString)
        val response =
          GetParameterValuesRequest(
            Seq(("InternetGatewayDevice.ManagementServer.Username"))
          )
        Future.successful(
          TransformationResult(response, ExpectGetParameterValuesResponse)
        )

      case (
          ExpectGetParameterValuesResponse,
          r: GetParameterValuesResponse
          ) =>
        log.info("Got GPVRes. Returning SPVReq. " + r.toString)
        val response = SetParameterValuesRequest()
        Future.successful(
          TransformationResult(response, ExpectSetParameterValuesResponse)
        )

      case (
          ExpectSetParameterValuesResponse,
          r: SetParameterValuesResponse
          ) =>
        log.info("Got SPVRes. Returning EM. " + r.toString)
        val response = EmptyResponse
        Future.successful(TransformationResult(response, ExpectInformRequest))

      case (s, r) =>
        log.error("Got weird stuff. " + s.toString + " and " + r.toString)
        Future.successful(
          TransformationResult(InvalidRequest, ExpectInformRequest)
        )
    }

}
