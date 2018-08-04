package com.github.freeacs.actors

sealed trait ConversationState

case class GoTo(state: ConversationState, data: ConversationData) extends ConversationState

case object WaitingForGoTo extends ConversationState

case object ExpectGoTo extends ConversationState

case object ExpectInformRequest extends ConversationState

case object ExpectEmptyRequest extends ConversationState

case object ExpectGetParameterNamesResponse extends ConversationState

case object ExpectGetParameterValuesResponse extends ConversationState

case object ExpectSetParameterValuesResponse extends ConversationState

case object ExpectRebootResponse extends ConversationState

case object Complete extends ConversationState

case object Failed extends ConversationState
