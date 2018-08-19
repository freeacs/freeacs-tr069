package com.github.freeacs.xml

sealed trait SOAPRequest

final case class InformRequest(
    deviceId: DeviceIdStruct,
    eventList: Seq[EventStruct],
    params: Seq[ParameterValueStruct]
) extends SOAPRequest {
  val msKey = "ManagementServer"

  val serialNumber = deviceId.serialNumber

  val keyRoot: Option[String] =
    params
      .map(p => {
        p.name.substring(0, p.name.indexOf(".") + 1)
      })
      .find(
        name => name.equals("Device.") || name.equals("InternetGatewayDevice.")
      )

  val softwareVersion        = getInformParam("DeviceInfo.SoftwareVersion")
  val periodicInformInterval = getInformParam(s"$msKey.PeriodicInformInterval")
  val connRequestUrl         = getInformParam(s"$msKey.ConnectionRequestURL")
  val connRequestUsername    = getInformParam(s"$msKey.ConnectionRequestUsername")
  val connRequestPassword    = getInformParam(s"$msKey.ConnectionRequestPassword")

  def getInformParam(key: String) =
    keyRoot
      .flatMap(
        kr => params.find(_.name == kr + key)
      )
      .map(_.value)
}

case class EmptyRequest() extends SOAPRequest

case class GetParameterNamesResponse(params: Seq[(String, Boolean)])
    extends SOAPRequest

case class GetParameterValuesResponse(params: Seq[(String, String)])
    extends SOAPRequest

case class SetParameterValuesResponse(status: Int) extends SOAPRequest
