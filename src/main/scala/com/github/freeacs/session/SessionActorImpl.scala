package com.github.freeacs.session

import com.github.freeacs.entities._
import com.github.freeacs.services.Tr069Services

import scala.concurrent.Future

class SessionActorImpl(user: String, services: Tr069Services) extends SessionActor {

  private var requests = Seq[SOAPRequest]()

  def request(request: SOAPRequest): Future[Option[SOAPResponse]] = request match {
    case inf: InformRequest =>
      requests = Seq(inf) // reset
      Future.successful(Some(InformResponse()))
    case EmptyRequest =>
      requests = requests :+ EmptyRequest
      Future.successful(None)
  }
}
