package com.github.freeacs.state
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

final case class Transformation(fn: (State, SOAPRequest) => State) {
  def apply(currentState: State, request: SOAPRequest): State =
    fn(currentState, request)
}

object Transformation {
  val log = LoggerFactory.getLogger(getClass)

  def transformer(user: String, services: Tr069Services) =
    Transformation {
      case (ExpectInformRequest(_), r: InformRequest) =>
        log.info("Got INReq. Returning INRes. " + r.toString)
        ExpectEmptyRequest(InformResponse())

      case (ExpectEmptyRequest(_), EmptyRequest) =>
        log.info("Got EM. Returning GPNReq.")
        val response = GetParameterNamesRequest("InternetGatewayDevice.")
        ExpectGetParameterNamesResponse(response)

      case (ExpectGetParameterNamesResponse(_), r: GetParameterNamesResponse) =>
        log.info("Got GPNRes. Returning GPVReq. " + r.toString)
        val response =
          GetParameterValuesRequest(
            Seq(("InternetGatewayDevice.ManagementServer.Username"))
          )
        ExpectGetParameterValuesResponse(response)

      case (
          ExpectGetParameterValuesResponse(_),
          r: GetParameterValuesResponse
          ) =>
        log.info("Got GPVRes. Returning SPVReq. " + r.toString)
        val response = SetParameterValuesRequest()
        ExpectSetParameterValuesResponse(response)

      case (
          ExpectSetParameterValuesResponse(_),
          r: SetParameterValuesResponse
          ) =>
        log.info("Got SPVRes. Returning EM. " + r.toString)
        val response = EmptyResponse
        ExpectInformRequest(response)

      case (s, r) =>
        log.error("Got weird stuff. " + s.toString + " and " + r.toString)
        ExpectInformRequest(InvalidRequest)
    }

}
