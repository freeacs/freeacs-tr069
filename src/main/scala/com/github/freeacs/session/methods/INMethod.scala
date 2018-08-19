package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{ExpectEmptyRequest, SessionState}
import com.github.freeacs.xml.{InformRequest, InformResponse, SOAPResponse}

import scala.concurrent.{ExecutionContext, Future}

object INMethod extends AbstractMethod[InformRequest] {
  def process(
      request: InformRequest,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    val cpeParams = InformParams(
      request.params.map(p => (p.name -> p.value)).toMap
    )
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

final case class InformParams(params: Map[String, String]) {

  lazy val keyRoot: Option[String] =
    params
      .map(p => p._1.substring(0, p._1.indexOf(".") + 1))
      .find(
        name => name.equals("Device.") || name.equals("InternetGatewayDevice.")
      )

  lazy val swVersion   = getParam("DeviceInfo.SoftwareVersion")
  lazy val perInfInt   = getParam("ManagementServer.PeriodicInformInterval")
  lazy val connReqUrl  = getParam("ManagementServer.ConnectionRequestURL")
  lazy val connReqUser = getParam("ManagementServer.ConnectionRequestUsername")
  lazy val connReqPass = getParam("ManagementServer.ConnectionRequestPassword")

  def getParam(key: String) =
    keyRoot
      .flatMap(
        kr => params.find(_._1 == kr + key)
      )
      .map(_._2)
}
