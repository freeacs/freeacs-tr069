package com.github.freeacs.xml

sealed trait SOAPRequest

final case class InformRequest(
  deviceId: DeviceIdStruct,
  eventList: Seq[EventStruct],
  params: Seq[ParameterValueStruct]
) extends SOAPRequest

case object EmptyRequest extends SOAPRequest

case class GetParameterNamesResponse(params: Seq[(String, String)]) extends SOAPRequest