package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.methods._
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

final case class SessionState(
    user: String,
    modified: Long,
    state: ExpectRequest,
    errorCount: Int,
    history: List[String]
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  def transition(services: Tr069Services, request: SOAPRequest)(
      implicit ec: ExecutionContext
  ): Future[(SessionState, SOAPResponse)] =
    (this, request) match {
      case (sessionState, request: InformRequest)
          if sessionState.state == ExpectInformRequest =>
        InformMethod.process(request, sessionState)

      case (sessionState, request: EmptyRequest)
          if sessionState.state == ExpectEmptyRequest =>
        EmptyMethod.process(request, sessionState)

      case (sessionState, request: GetParameterNamesResponse)
          if sessionState.state == ExpectGetParameterNamesResponse =>
        GPNMethod.process(request, sessionState)

      case (sessionState, request: GetParameterValuesResponse)
          if sessionState.state == ExpectGetParameterValuesResponse =>
        GPVMethod.process(request, sessionState)

      case (sessionState, request: SetParameterValuesResponse)
          if sessionState.state == ExpectSetParameterValuesResponse =>
        SPVMethod.process(request, sessionState)

      case (sessionState, _) =>
        val newState =
          if (sessionState.errorCount < 2)
            sessionState.copy(
              errorCount = (sessionState.errorCount + 1)
            )
          else
            sessionState.copy(
              errorCount = 0,
              state = ExpectInformRequest
            )
        Future.successful((newState, InvalidRequest()))
    }
}
