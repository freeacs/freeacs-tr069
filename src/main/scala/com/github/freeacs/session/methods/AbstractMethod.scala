package com.github.freeacs.session.methods
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionState
import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

trait AbstractMethod[T <: SOAPRequest] {

  val log = LoggerFactory.getLogger(getClass)

  def process(
      request: T,
      state: SessionState,
      services: Tr069Services
  )(implicit ec: ExecutionContext): Future[(SessionState, SOAPResponse)]
}
