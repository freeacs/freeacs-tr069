package com.github.freeacs.session.methods
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.freeacs.config.SystemParameters._
import com.github.freeacs.domain.{UnitParameter, UnitTypeParameter}
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionState._
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
      .map { unit =>
        sessionState.copy(
          serialNumber = Option(request.deviceId.serialNumber),
          softwareVersion = cpeParams.swVersion.map(_.value),
          unitTypeId = unit.flatMap(_.unitType.unitTypeId),
          profileId = unit.flatMap(_.profile.profileId),
          unitParams = unit
            .map(_.params.map(toUnitParameterTuple).toList)
            .getOrElse(List.empty),
          unitTypeParams = unit
            .map(_.unitType.params.map(toUnitTypeParameterTuple).toList)
            .getOrElse(List.empty)
        )
      }
      .flatMap { state =>
        savePeriodicInform(services, cpeParams, state).map { result =>
          log.info(s"Updated or created $result unit parameters")
          state
        }
      }
      .map(state => {
        log.info("Got INReq. Returning INRes. " + request.toString)
        (
          state.copy(
            state = ExpectEmptyRequest,
            history = (state.history :+ ("INReq", "INRes"))
          ),
          InformResponse()
        )
      })
  }

  private def toUnitParameterTuple(
      p: UnitParameter
  ): UnitParameterType = {
    (
      p.unitTypeParameter.unitTypeParamId,
      p.unitTypeParameter.name,
      p.value
    )
  }
  private def toUnitTypeParameterTuple(
      p: UnitTypeParameter
  ): UnitTypeParameterType = {
    (
      p.unitTypeParamId,
      p.name,
      p.flags,
      p.unitTypeId
    )
  }

  private def savePeriodicInform(
      services: Tr069Services,
      cpeParams: InformParams,
      state: SessionState
  ): Future[Int] = {
    services.createOrUpdateUnitParameters {
      val currentTimestamp = LocalDateTime
        .now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      List(
        mkParameter(
          state.user,
          state.unitTypeParams,
          LAST_CONNECT_TMS,
          currentTimestamp
        ),
        state.unitParams
          .find(_._2 == FIRST_CONNECT_TMS)
          .map(_ => List.empty)
          .getOrElse(
            mkParameter(
              state.user,
              state.unitTypeParams,
              FIRST_CONNECT_TMS,
              currentTimestamp
            )
          ),
        mkParameter(
          state.user,
          state.unitTypeParams,
          SERIAL_NUMBER,
          state.serialNumber.getOrElse("")
        ),
        mkParameter(
          state.user,
          state.unitTypeParams,
          SOFTWARE_VERSION,
          cpeParams.perInfInt.map(_.value).getOrElse("")
        ),
        mkParameter(
          state.user,
          state.unitTypeParams,
          IP_ADDRESS,
          state.remoteAddress
        ),
        mkParameter(
          state.user,
          state.unitTypeParams,
          PERIODIC_INTERVAL,
          cpeParams.perInfInt.map(_.value).getOrElse("")
        ),
        mkParameter(state.user, state.unitTypeParams, cpeParams.swVersion),
        mkParameter(state.user, state.unitTypeParams, cpeParams.perInfInt),
        mkParameter(state.user, state.unitTypeParams, cpeParams.connReqUrl),
        mkParameter(state.user, state.unitTypeParams, cpeParams.connReqUser),
        mkParameter(state.user, state.unitTypeParams, cpeParams.connReqPass)
      ).flatten
    }
  }

  private def mkParameter(
      user: String,
      unitTypeParams: List[UnitTypeParameterType],
      param: String,
      value: String
  ): List[(String, String, Long)] = {
    unitTypeParams
      .find(p => p._2 == param)
      .map {
        case (Some(utpId), _, _, _) =>
          List((user, value, utpId))
        case _ => List.empty
      }
      .getOrElse(List.empty)
  }

  private def mkParameter(
      user: String,
      unitTypeParams: List[UnitTypeParameterType],
      param: Option[ParameterValueStruct]
  ): List[(String, String, Long)] = {
    param.flatMap { param =>
      unitTypeParams.find(p => p._2 == param.name).map {
        case (Some(utpId), _, _, _) =>
          List((user, param.value, utpId))
        case _ => List.empty
      }
    }.getOrElse(List.empty)
  }

  private case class InformParams(params: Seq[ParameterValueStruct]) {

    lazy val keyRoot: Option[String] =
      params
        .map(p => p.name.substring(0, p.name.indexOf(".") + 1))
        .find(
          name =>
            name.equals("Device.") || name.equals("InternetGatewayDevice.")
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
}
