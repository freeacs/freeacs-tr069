package com.github.freeacs.xml

sealed trait SOAPRequest

final case class InformRequest(
    deviceId: DeviceIdStruct,
    eventList: Seq[EventStruct],
    params: Seq[ParameterValueStruct]
) extends SOAPRequest {
  val serialNumber = Option(deviceId.serialNumber)
}

case class EmptyRequest() extends SOAPRequest

case class GetParameterNamesResponse(params: Seq[(String, Boolean)])
    extends SOAPRequest

case class GetParameterValuesResponse(params: Seq[(String, String)])
    extends SOAPRequest

case class SetParameterValuesResponse(status: Int) extends SOAPRequest
