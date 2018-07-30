package com.github.freeacs.entities

final case class EventStruct(
  eventCode: String,
  commandKey: String
) {
  val factoryReset = eventCode.startsWith("0")
  val booted = eventCode.startsWith("1")
  val periodic = eventCode.startsWith("2")
  val valueChange = eventCode.startsWith("4")
  val kicked = eventCode.startsWith("6")
  val diagnosticsComplete = eventCode.startsWith("8")
}