// Copyright (C) 2014-2017 Anduin Transactions Inc.
// Heavily modified for FreeACS

package com.github.freeacs.state

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.xml.SOAPRequest

final case class FSM(currentState: State) extends ReplicatedData {
  def transition(request: SOAPRequest, transform: Transformation): FSM = {
    FSM(transform(currentState, request))
  }
  override type T = FSM
  override def merge(that: FSM): FSM =
    FSM(that.currentState)
}
