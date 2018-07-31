package com.github.freeacs.session

import com.github.freeacs.entities._
import com.github.freeacs.services.Tr069Services

import scala.collection.mutable
import scala.concurrent.Future

class SessionActorImpl(user: String, services: Tr069Services) extends SessionActor {

  private var requests: mutable.Map[SOAPRequest, Option[SOAPResponse]] = mutable.LinkedHashMap()

  def request(request: SOAPRequest): Future[Option[SOAPResponse]] = request match {
    case inform: InformRequest =>
      val response = Some(InformResponse())
      requests = mutable.Map()
      requests.put(inform, response)
      println(requests)
      Future.successful(response)
    case EmptyRequest =>
      requests.put(EmptyRequest, None)
      println(requests)
      Future.successful(None)
  }
}
