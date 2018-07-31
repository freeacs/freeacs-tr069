package com.github.freeacs.session

import com.github.freeacs.entities.{SOAPRequest, SOAPResponse}

import scala.concurrent.Future

trait SessionActor {

  def request(request: SOAPRequest): Future[SOAPResponse]
}
