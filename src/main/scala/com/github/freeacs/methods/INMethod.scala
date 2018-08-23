package com.github.freeacs.methods
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.History
import com.github.freeacs.session.sessionState.SessionState.HistoryItem.{
  INReq,
  INRes
}
import com.github.freeacs.session.sessionState.SessionState.State.ExpectEmptyRequest
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
      services: DaoService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    val cpeParams = InformParams(request.params)
    services
      .getUnit(sessionState.user)
      .map { unit =>
        sessionState
      }
      .map(state => {
        log.info("Got INReq. Returning INRes. " + request.toString)
        (
          state.copy(
            state = ExpectEmptyRequest,
            history = state.history :+ History(INReq, INRes)
          ),
          InformResponse()
        )
      })
  }

  private[this] case class InformParams(params: Seq[ParameterValueStruct]) {

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
