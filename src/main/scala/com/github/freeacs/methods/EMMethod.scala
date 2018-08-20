package com.github.freeacs.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{
  ExpectGetParameterNamesResponse,
  SessionState
}
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
}
