package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{ExpectEmptyRequest, SessionState}
import com.github.freeacs.xml.{
  InformRequest,
  InformResponse,
  ParameterValueStruct,
  SOAPResponse
}

import scala.concurrent.{ExecutionContext, Future}

object INMethod extends AbstractMethod[InformRequest] {
  def process(
      request: InformRequest,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    val cpeParams = InformParams(request.params)
    services
      .getUnit(sessionState.user)
      .map(
        _.map(
          unit =>
            sessionState.copy(
              unitTypeId = unit.unitType.unitTypeId,
              profileId = unit.profile.profileId
          )
        ).getOrElse(sessionState)
      )
      .map { state =>
        log.info("Got INReq. Returning INRes. " + request.toString)
        log.info("Params: " + cpeParams)
        (
          state.copy(
            state = ExpectEmptyRequest,
            history = (state.history :+ ("INReq", "INRes")),
            softwareVersion = cpeParams.swVersion,
            serialNumber = Option(request.deviceId.serialNumber)
          ),
          InformResponse()
        )
      }
  }
}

final case class InformParams(params: Seq[ParameterValueStruct]) {

  lazy val keyRoot: Option[String] =
    params
      .map(p => p.name.substring(0, p.name.indexOf(".") + 1))
      .find(
        name => name.equals("Device.") || name.equals("InternetGatewayDevice.")
      )

  lazy val swVersion   = getValue("DeviceInfo.SoftwareVersion")
  lazy val perInfInt   = getValue("ManagementServer.PeriodicInformInterval")
  lazy val connReqUrl  = getValue("ManagementServer.ConnectionRequestURL")
  lazy val connReqUser = getValue("ManagementServer.ConnectionRequestUsername")
  lazy val connReqPass = getValue("ManagementServer.ConnectionRequestPassword")

  private[this] def getValue(key: String) =
    keyRoot
      .flatMap(
        kr => params.find(_.name == kr + key)
      )
      .map(_.value)
}
