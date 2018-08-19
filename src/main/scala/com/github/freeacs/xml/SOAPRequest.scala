package com.github.freeacs.xml

sealed trait SOAPRequest

final case class InformParams(params: Map[String, String]) {

  lazy val keyRoot: Option[String] =
    params
      .map(p => p._1.substring(0, p._1.indexOf(".") + 1))
      .find(
        name => name.equals("Device.") || name.equals("InternetGatewayDevice.")
      )

  lazy val swVersion   = getParam("DeviceInfo.SoftwareVersion")
  lazy val perInfInt   = getParam("ManagementServer.PeriodicInformInterval")
  lazy val connReqUrl  = getParam("ManagementServer.ConnectionRequestURL")
  lazy val connReqUser = getParam("ManagementServer.ConnectionRequestUsername")
  lazy val connReqPass = getParam("ManagementServer.ConnectionRequestPassword")

  def getParam(key: String) =
    keyRoot
      .flatMap(
        kr => params.find(_._1 == kr + key)
      )
      .map(_._2)
}

final case class InformRequest(
    deviceId: DeviceIdStruct,
    eventList: Seq[EventStruct],
    params: Seq[ParameterValueStruct]
) extends SOAPRequest {
  lazy val cpeParams = InformParams(
    params.map(p => (p.name -> p.value)).toMap
  )
}

final case class EmptyRequest() extends SOAPRequest

final case class GetParameterNamesResponse(params: Seq[(String, Boolean)])
    extends SOAPRequest

final case class GetParameterValuesResponse(params: Seq[(String, String)])
    extends SOAPRequest

final case class SetParameterValuesResponse(status: Int) extends SOAPRequest
