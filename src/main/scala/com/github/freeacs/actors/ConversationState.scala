package com.github.freeacs.actors

sealed trait ConversationState

case object WaitingForResponse extends ConversationState

case object ExpectInform extends ConversationState

case class GoTo(state: ConversationState, data: ConversationData) extends ConversationState

case object ExpectEmpty extends ConversationState

case object Complete extends ConversationState

case object Failed extends ConversationState
