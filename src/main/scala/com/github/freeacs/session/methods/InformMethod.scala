package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.{ExpectEmptyRequest, SessionState}
import com.github.freeacs.xml.{InformRequest, InformResponse, SOAPResponse}

import scala.concurrent.{ExecutionContext, Future}

object InformMethod extends AbstractMethod[InformRequest] {
  def process(
      request: InformRequest,
      sessionState: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)] = {
    log.info("Got INReq. Returning INRes. " + request.toString)
    Future.successful(
      (
        sessionState.copy(
          state = ExpectEmptyRequest,
          history = (sessionState.history :+ ("INReq", "INRes"))
        ),
        InformResponse()
      )
    )
  }
}
