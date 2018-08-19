package com.github.freeacs.session.methods
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.freeacs.config.SystemParameters
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{
  ExpectGetParameterNamesResponse,
  SessionState
}
import com.github.freeacs.vo.ParameterValueVO
import com.github.freeacs.xml.{
  EmptyRequest,
  GetParameterNamesRequest,
  SOAPResponse
}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object EMMethod extends AbstractMethod[EmptyRequest] {
  def process(
      request: EmptyRequest,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] =
    Future.successful {
      if (sessionState.history.isEmpty) {
        log.error(
          "EM-Decision is EM since the CPE did not send an INFORM (or sessionId was not sent by client)"
        )
        resetConversation(sessionState)
      } else if (sessionState.history.last._1 == "EM") {
        log.info("EM-Decision is EM since two last responses from CPE was EM")
        resetConversation(sessionState)
      } else if (Seq("INReq", "TCReq", "GRMRes").contains(
                   sessionState.history.last._1
                 )) {
        if (sessionState.unitTypeId.isEmpty) {
          log.info("EM-Decision is EM since unit is not found")
          resetConversation(sessionState)
        } else {
          getSystemParameters(sessionState, services).onComplete {
            case Success(value) =>
              log.info(s"Found ${value.size} system parameters: $value")
            case Failure(exception) =>
              log.error("Failed to retrieve system parameters", exception)
          }
          log.info("TODO: Got EM. Default behaviour is to return GPNReq.")
          val response = GetParameterNamesRequest("InternetGatewayDevice.")
          (
            sessionState.copy(
              state = ExpectGetParameterNamesResponse,
              history = (sessionState.history :+ ("EM", "GPNReq"))
            ),
            response
          )
        }
      } else {
        log.info(
          "EM-Decision is EM since it is the default method choice (nothing else fits)"
        )
        resetConversation(sessionState)
      }
    }

  private def getSystemParameters(
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[List[ParameterValueVO]] = {
    services.getUnitParameters(sessionState.user).map { unitParams =>
      var systemParameters = List[ParameterValueVO]()

      val currentTimestamp = LocalDateTime
        .now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

      systemParameters = systemParameters :+ ParameterValueVO(
        SystemParameters.LAST_CONNECT_TMS,
        currentTimestamp
      )

      systemParameters = systemParameters :+ ParameterValueVO(
        SystemParameters.FIRST_CONNECT_TMS,
        unitParams
          .find(
            _.unitTypeParameter.name == SystemParameters.FIRST_CONNECT_TMS
          )
          .flatMap(_.value)
          .getOrElse(currentTimestamp)
      )

      systemParameters = systemParameters :+ ParameterValueVO(
        SystemParameters.IP_ADDRESS,
        sessionState.remoteAddress
      )

      systemParameters = systemParameters :+ ParameterValueVO(
        SystemParameters.SOFTWARE_VERSION,
        sessionState.softwareVersion.getOrElse("")
      )

      systemParameters = systemParameters :+ ParameterValueVO(
        SystemParameters.SERIAL_NUMBER,
        sessionState.serialNumber.getOrElse("")
      )

      systemParameters
    }
  }
}
