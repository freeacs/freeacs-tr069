package com.github.freeacs.xml

final case class ParameterValueStruct(name: String, value: String)

object ParameterValueStruct {
  def getKeyRoot(params: Seq[ParameterValueStruct]): Option[String] = params.map(p => {
    p.name.substring(0, p.name.indexOf(".") + 1)
  }).find(name => name.equals("Device.") || name.equals("InternetGatewayDevice."))
}