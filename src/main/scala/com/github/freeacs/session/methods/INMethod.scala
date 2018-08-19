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
        log.info("Params: " + request.cpeParams)
        (
          state.copy(
            state = ExpectEmptyRequest,
            history = (state.history :+ ("INReq", "INRes")),
            softwareVersion = request.cpeParams.swVersion,
            serialNumber = Option(request.deviceId.serialNumber)
          ),
          InformResponse()
        )
      }
  }
}
