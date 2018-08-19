package com.github.freeacs.session.methods
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.freeacs.config.SystemParameters._
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
      .map { unit =>
        sessionState.copy(
          serialNumber = Option(request.deviceId.serialNumber),
          unitTypeId = unit.flatMap(_.unitType.unitTypeId),
          profileId = unit.flatMap(_.profile.profileId),
          unitTypeParams = unit
            .map(
              _.unitType.params.map { p =>
                (
                  p.unitTypeParamId,
                  p.name,
                  p.flags,
                  p.unitTypeId
                )
              }.toList
            )
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
            history = (state.history :+ ("INReq", "INRes")),
            softwareVersion = cpeParams.swVersion.map(_.value),
            serialNumber = Option(request.deviceId.serialNumber)
          ),
          InformResponse()
        )
      })
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
          state,
          LAST_CONNECT_TMS,
          currentTimestamp
        ),
        mkParameter(
          state,
          SERIAL_NUMBER,
          state.serialNumber.getOrElse("")
        ),
        mkParameter(
          state,
          SOFTWARE_VERSION,
          cpeParams.perInfInt.map(_.value).getOrElse("")
        ),
        mkParameter(
          state,
          IP_ADDRESS,
          state.remoteAddress
        ),
        mkParameter(
          state,
          PERIODIC_INTERVAL,
          cpeParams.perInfInt.map(_.value).getOrElse("")
        ),
        mkParameter(state, cpeParams.swVersion),
        mkParameter(state, cpeParams.perInfInt),
        mkParameter(state, cpeParams.connReqUrl),
        mkParameter(state, cpeParams.connReqUser),
        mkParameter(state, cpeParams.connReqPass)
      ).flatten
    }
  }

  private def mkParameter(
      state: SessionState,
      param: String,
      value: String
  ): List[(String, String, Long)] = {
    state.unitTypeParams
      .find(p => p._2 == param)
      .map {
        case (Some(utpId), _, _, _) =>
          List(
            (
              state.user,
              value,
              utpId
            )
          )
        case _ => List.empty
      }
      .getOrElse(List.empty)
  }

  private def mkParameter(
      state: SessionState,
      param: Option[ParameterValueStruct]
  ): List[(String, String, Long)] = {
    param.flatMap { param =>
      state.unitTypeParams.find(p => p._2 == param.name).map {
        case (Some(utpId), _, _, _) =>
          List(
            (
              state.user,
              param.value,
              utpId
            )
          )
        case _ => List.empty
      }
    }.getOrElse(List.empty)
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
