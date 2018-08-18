package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.SessionProtocol.TransformationResult
import com.github.freeacs.xml.SOAPRequest

import scala.concurrent.{ExecutionContext, Future}

final case class SessionState(
    user: String,
    modified: Long,
    state: ExpectRequest,
    errorCount: Int = 0
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  def transition(
      user: String,
      services: Tr069Services,
      request: SOAPRequest
  )(implicit ec: ExecutionContext): Future[TransformationResult] =
    SessionProtocol.protocol(user, services).transform(this, request)

}
