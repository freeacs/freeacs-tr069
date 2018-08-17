// Copyright (C) 2014-2017 Anduin Transactions Inc.
// Heavily modified for FreeACS

package com.github.freeacs.state

import com.github.freeacs.xml.SOAPRequest

import scala.concurrent.{ExecutionContext, Future}

final case class FSM(currentState: State) {
  def transition(
      request: SOAPRequest,
      transform: Transformation
  )(implicit ec: ExecutionContext): Future[TransformationResult] =
    transform(currentState, request)
}
