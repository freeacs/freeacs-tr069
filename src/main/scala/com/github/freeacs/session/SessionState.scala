package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.session.methods._
import com.github.freeacs.xml._
import com.github.freeacs.domain
import scala.concurrent.{ExecutionContext, Future}

final case class SessionState(
    user: String,
    modified: Long,
    state: State,
    errorCount: Int = 0,
    remoteAddress: String,
    history: List[(String, String)] = List.empty,
    unitTypeId: Option[Long] = None,
    profileId: Option[Long] = None,
    softwareVersion: Option[String] = None,
    serialNumber: Option[String] = None,
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  def transition(services: Tr069Services, request: SOAPRequest)(
      implicit ec: ExecutionContext
  ): Future[(SessionState, SOAPResponse)] = request match {
    case request: InformRequest if state == ExpectInformRequest =>
      INMethod.process(request, this, services)

    case request: EmptyRequest if state == ExpectEmptyRequest =>
      EMMethod.process(request, this, services)

    case request: GetParameterNamesResponse
        if state == ExpectGetParameterNamesResponse =>
      GPNMethod.process(request, this, services)

    case request: GetParameterValuesResponse
        if state == ExpectGetParameterValuesResponse =>
      GPVMethod.process(request, this, services)

    case request: SetParameterValuesResponse
        if state == ExpectSetParameterValuesResponse =>
      SPVMethod.process(request, this, services)

    case _ =>
      val newState =
        if (errorCount < 2)
          copy(errorCount = (errorCount + 1))
        else
          copy(
            errorCount = 0,
            state = ExpectInformRequest,
            history = List.empty
          )
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
