package com.github.freeacs.xml

final case class EventStruct(
  eventCode: String,
  commandKey: String
) {
  val factoryReset: Boolean = eventCode.startsWith("0")
  val booted: Boolean = eventCode.startsWith("1")
  val periodic: Boolean = eventCode.startsWith("2")
  val valueChange: Boolean = eventCode.startsWith("4")
  val kicked: Boolean = eventCode.startsWith("6")
  val diagnosticsComplete: Boolean = eventCode.startsWith("8")
}