package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.session.SessionState._
import com.github.freeacs.methods._
import com.github.freeacs.repositories.DaoService
import com.github.freeacs.xml._

import scala.concurrent.{ExecutionContext, Future}

object SessionState {
  type UnitParameterType     = (Option[Long], String, Option[String])
  type UnitTypeParameterType = (Option[Long], String, String, Long)
  type HistoryType           = (HistoryItem, HistoryItem)
}

final case class SessionState(
    user: String,
    modified: Long,
    state: State,
    remoteAddress: String,
    errorCount: Int = 0,
    history: List[HistoryType] = List.empty,
    unitTypeId: Option[Long] = None,
    profileId: Option[Long] = None,
    softwareVersion: Option[String] = None,
    serialNumber: Option[String] = None,
    unitParams: List[UnitParameterType] = List.empty,
    unitTypeParams: List[UnitTypeParameterType] = List.empty
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  def transition(services: DaoService, request: SOAPRequest)(
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

sealed trait HistoryItem
case object EM     extends HistoryItem
case object INRes  extends HistoryItem
case object INReq  extends HistoryItem
case object GPNRes extends HistoryItem
case object GPNReq extends HistoryItem
case object GPVRes extends HistoryItem
case object GPVReq extends HistoryItem
case object SPVRes extends HistoryItem
case object SPVReq extends HistoryItem
