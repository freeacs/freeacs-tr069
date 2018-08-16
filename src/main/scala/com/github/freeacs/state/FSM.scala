// Copyright (C) 2014-2017 Anduin Transactions Inc.
// Heavily modified for FreeACS

package com.github.freeacs.state

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.xml.SOAPRequest

import scala.concurrent.{ExecutionContext, Future}

final case class FSM(currentState: State) extends ReplicatedData {
  def transition(
      request: SOAPRequest,
      transform: Transformation
  )(implicit ec: ExecutionContext): Future[FSM] =
    transform(currentState, request).map(FSM.apply)

  override type T = FSM
  override def merge(that: FSM): FSM =
    FSM(that.currentState)
}
