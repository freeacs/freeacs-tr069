package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.methods._
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

final case class SessionState(
    user: String,
    modified: Long,
    state: State,
    errorCount: Int,
    history: List[(String, String)]
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  def transition(services: Tr069Services, request: SOAPRequest)(
      implicit ec: ExecutionContext
  ): Future[(SessionState, SOAPResponse)] = request match {
    case request: InformRequest if state == ExpectInformRequest =>
      InformMethod.process(request, this)

    case request: EmptyRequest if state == ExpectEmptyRequest =>
      EmptyMethod.process(request, this)

    case request: GetParameterNamesResponse
        if state == ExpectGetParameterNamesResponse =>
      GPNMethod.process(request, this)

    case request: GetParameterValuesResponse
        if state == ExpectGetParameterValuesResponse =>
      GPVMethod.process(request, this)

    case request: SetParameterValuesResponse
        if state == ExpectSetParameterValuesResponse =>
      SPVMethod.process(request, this)

    case _ =>
      val newState =
        if (errorCount < 2)
          copy(errorCount = (errorCount + 1))
        else
          copy(errorCount = 0, state = ExpectInformRequest)
      Future.successful((newState, InvalidRequest()))
  }
}

sealed trait State
case object ExpectInformRequest              extends State
case object ExpectEmptyRequest               extends State
case object ExpectGetParameterNamesResponse  extends State
case object ExpectGetParameterValuesResponse extends State
case object ExpectSetParameterValuesResponse extends State
case object ExpectRebootResponse             extends State
