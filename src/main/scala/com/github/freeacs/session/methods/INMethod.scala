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
      .map {
        case Some(unit) =>
          services
            .getUnitTypeParameters(unit.unitType.unitTypeId.get)
            .map(params => (Some(unit), params))
        case _ =>
          Future.successful((None, Seq.empty))
      }
      .flatMap(
        _.map(
          result =>
            sessionState.copy(
              unitTypeId = result._1.flatMap(_.unitType.unitTypeId),
              profileId = result._1.flatMap(_.profile.profileId),
              unitTypeParams = result._2.toList
          )
        )
      )
      .flatMap { state =>
        if (state.unitTypeId.isDefined && state.unitTypeParams.nonEmpty) {
          services
            .createOrUpdateUnitParameters(
              Seq(
                (
                  sessionState.user,
                  cpeParams.perInfInt.map(_.value).getOrElse(""),
                  sessionState.unitTypeParams
                    .find(_._2 == cpeParams.perInfInt.map(_.name))
                    .flatMap(_._1)
                    .get
                )
              )
            )
            .map(_ => state)
        } else {
          Future.successful(state)
        }
      }
      .map { state =>
        log.info("Got INReq. Returning INRes. " + request.toString)
        log.info("Params: " + cpeParams)
        (
          state.copy(
            state = ExpectEmptyRequest,
            history = (state.history :+ ("INReq", "INRes")),
            softwareVersion = cpeParams.swVersion.map(_.value),
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

  lazy val swVersionKey = keyRoot.map(kr => kr + "DeviceInfo.SoftwareVersion")
  lazy val perInfIntKey =
    keyRoot.map(kr => kr + "ManagementServer.PeriodicInformInterval")
  lazy val connReqUrlKey =
    keyRoot.map(kr => kr + "ManagementServer.ConnectionRequestURL")
  lazy val connReqUserKey =
    keyRoot.map(kr => kr + "ManagementServer.ConnectionRequestUsername")
  lazy val connReqPassKey =
    keyRoot.map(kr => kr + "ManagementServer.ConnectionRequestPassword")

  lazy val swVersion   = getValue(swVersionKey)
  lazy val perInfInt   = getValue(perInfIntKey)
  lazy val connReqUrl  = getValue(connReqUrlKey)
  lazy val connReqUser = getValue(connReqUserKey)
  lazy val connReqPass = getValue(connReqPassKey)

  private[this] def getValue(key: Option[String]) =
    key.flatMap(
      k => params.find(_.name == k)
    )
}
