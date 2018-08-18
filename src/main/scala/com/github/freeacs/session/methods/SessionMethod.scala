package com.github.freeacs.session.methods
import com.github.freeacs.session.SessionState
import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

trait SessionMethod[T <: SOAPRequest] {

  val log = LoggerFactory.getLogger(getClass)

  def process(
      request: T,
      state: SessionState
  ): Future[(SessionState, SOAPResponse)]
}
