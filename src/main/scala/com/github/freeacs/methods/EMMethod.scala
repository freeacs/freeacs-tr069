package com.github.freeacs.methods
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.freeacs.config.SystemParameters._
import com.github.freeacs.domain.ACSUnitParameter.ACSUnitParameterTupleType
import com.github.freeacs.domain.ACSUnitTypeParameter.ACSUnitTypeParameterTupleType
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.session._
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
      services: DaoService
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    if (sessionState.history.isEmpty) {
      log.error(
        "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)"
      )
      Future.successful(resetConversation(sessionState))
    } else {
      val previousMethod = sessionState.history.last._1
      if (previousMethod == EM) {
        log.info("EM-Decision is EM since two last responses from CPE was EM")
        Future.successful(resetConversation(sessionState))
      } else if (Seq(INReq).contains(previousMethod)) {
        if (sessionState.unitTypeId.isEmpty) {
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
                history = (sessionState.history :+ (EM, GPNReq))
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
      services: DaoService,
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
          .find(_._2 == FIRST_CONNECT_TMS)
          .flatMap(p => state.unitParams.find(up => p._4.contains(up._2)))
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
      unitTypeParams: List[ACSUnitTypeParameterTupleType],
      param: String,
      value: String
  ): List[ACSUnitParameterTupleType] = {
    unitTypeParams
      .find(p => p._1 == param)
      .map {
        case (_, _, _, Some(utpId)) =>
          List(
            (
              user,
              utpId,
              Some(value)
            )
          )
        case _ => List.empty
      }
      .getOrElse(List.empty)
  }
}
