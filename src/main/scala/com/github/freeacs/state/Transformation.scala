package com.github.freeacs.state
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

final case class Transformation(fn: (State, SOAPRequest) => Future[State]) {
  def apply(currentState: State, request: SOAPRequest): Future[State] =
    fn(currentState, request)
}

object Transformation {
  val log = LoggerFactory.getLogger(getClass)

  def transformer(user: String, services: Tr069Services) =
    Transformation {
      case (ExpectInformRequest(_), r: InformRequest) =>
        log.info("Got INReq. Returning INRes. " + r.toString)
        Future.successful(ExpectEmptyRequest(InformResponse()))

      case (ExpectEmptyRequest(_), EmptyRequest) =>
        log.info("Got EM. Returning GPNReq.")
        val response = GetParameterNamesRequest("InternetGatewayDevice.")
        Future.successful(ExpectGetParameterNamesResponse(response))

      case (ExpectGetParameterNamesResponse(_), r: GetParameterNamesResponse) =>
        log.info("Got GPNRes. Returning GPVReq. " + r.toString)
        val response =
          GetParameterValuesRequest(
            Seq(("InternetGatewayDevice.ManagementServer.Username"))
          )
        Future.successful(ExpectGetParameterValuesResponse(response))

      case (
          ExpectGetParameterValuesResponse(_),
          r: GetParameterValuesResponse
          ) =>
        log.info("Got GPVRes. Returning SPVReq. " + r.toString)
        val response = SetParameterValuesRequest()
        Future.successful(ExpectSetParameterValuesResponse(response))

      case (
          ExpectSetParameterValuesResponse(_),
          r: SetParameterValuesResponse
          ) =>
        log.info("Got SPVRes. Returning EM. " + r.toString)
        val response = EmptyResponse
        Future.successful(ExpectInformRequest(response))

      case (s, r) =>
        log.error("Got weird stuff. " + s.toString + " and " + r.toString)
        Future.successful(ExpectInformRequest(InvalidRequest))
    }

}
