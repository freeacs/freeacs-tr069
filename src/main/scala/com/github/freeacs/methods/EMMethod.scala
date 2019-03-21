package com.github.freeacs.methods
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.freeacs.config.SystemParameters._
import com.github.freeacs.domain.unitParameter
import com.github.freeacs.domain.unitParameter.ACSUnitParameter
import com.github.freeacs.domain.unitTypeParameter.ACSUnitTypeParameter
import com.github.freeacs.services.UnitService
import com.github.freeacs.session.sessionState.SessionState
import com.github.freeacs.session.sessionState.SessionState.History
import com.github.freeacs.session.sessionState.SessionState.HistoryItem.{
  EM,
  GPNReq,
  INReq
}
import com.github.freeacs.session.sessionState.SessionState.State.ExpectGetParameterNamesResponse
import com.github.freeacs.xml.{
  EmptyRequest,
  GetParameterNamesRequest,
  SOAPResponse
}

import scala.concurrent.{ExecutionContext, Future}

object EMMethod extends AbstractMethod[EmptyRequest] {
  def process(
      request: EmptyRequest,
      sessionState: SessionState,
      services: UnitService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    if (sessionState.history.isEmpty) {
      log.error(
        "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)"
      )
      Future.successful(resetConversation(sessionState))
    } else {
      val previousMethod = sessionState.history.last.request
      if (previousMethod == EM) {
        log.info("EM-Decision is EM since two last responses from CPE was EM")
        Future.successful(resetConversation(sessionState))
      } else if (Seq(INReq).contains(previousMethod)) {
        if (sessionState.acsUnit.isEmpty) {
          log.info("EM-Decision is EM since unit is not found")
          Future.successful(resetConversation(sessionState))
        } else {
          writeSystemParameters(services, sessionState).map { result =>
            log.info(s"Updated or created $result unit parameters")
            log.info("Got EM. Default behaviour is to return GPNReq.")
            val response = GetParameterNamesRequest("InternetGatewayDevice.")
            (
              sessionState.copy(
                state = ExpectGetParameterNamesResponse,
                history = sessionState.history :+ History(EM, GPNReq)
              ),
              response
            )
          }
        }
      } else {
        log.info(
          "EM-Decision is EM since it is the default method choice (nothing else fits)"
        )
        Future.successful(resetConversation(sessionState))
      }
    }
  }

  private[this] def writeSystemParameters(
      services: UnitService,
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
        state.unitTypeParams
          .find(_.name == FIRST_CONNECT_TMS)
          .flatMap(
            p =>
              state.unitParams.find(
                up => p.name.contains(up.unitTypeParameter.name)
            )
          )
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
          state.softwareVersion.getOrElse("")
        ),
        mkParameter(
          state.user,
          state.unitTypeParams,
          IP_ADDRESS,
          state.remoteAddress
        )
      ).flatten
    }
  }

  private[this] def mkParameter(
      user: String,
      unitTypeParams: Seq[ACSUnitTypeParameter],
      param: String,
      value: String
  ): List[ACSUnitParameter] = {
    unitTypeParams
      .find(p => p.name == param)
      .map { utp =>
        List(
          unitParameter.ACSUnitParameter(
            user,
            utp,
            Some(value)
          )
        )
      }
      .getOrElse(List.empty)
  }
}
