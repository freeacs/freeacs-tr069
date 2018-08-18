package com.github.freeacs.session.methods
import com.github.freeacs.session.{ExpectEmptyRequest, SessionState}
import com.github.freeacs.xml.{InformRequest, InformResponse, SOAPResponse}

import scala.concurrent.Future

object InformMethod extends SessionMethod[InformRequest] {
  def process(
      request: InformRequest,
      sessionState: SessionState
  ): Future[(SessionState, SOAPResponse)] = {
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
