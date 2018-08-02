package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{DeviceIdStruct, EventStruct, ParameterValueStruct}

import scala.collection.immutable
import scala.xml.Elem

object EnvelopeXML {
  def withEnvelope(xml: Elem): Elem =
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                      xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
                      xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns:cwmp="urn:dslforum-org:cwmp-1-0">
      <soapenv:Header>
        <cwmp:ID soapenv:mustUnderstand="1">1</cwmp:ID>
      </soapenv:Header>
      <soapenv:Body>
        {xml}
      </soapenv:Body>
    </soapenv:Envelope>

  def parseParameterValueStructs(xml: Elem): immutable.Seq[ParameterValueStruct] =
    (xml \\ "ParameterValueStruct").seq
      .map(p => ParameterValueStruct((p \\ "Name").text, (p \\ "Value").text))

  def parseEventStructs(xml: Elem): immutable.Seq[EventStruct] =
    (xml \\ "EventStruct").seq
      .map(p => EventStruct((p \\ "EventCode").text, (p \\ "CommandKey").text))

  def parseDeviceIdStruct(xml: Elem): DeviceIdStruct = {
    val deviceIdElem = xml \\ "DeviceId"
    DeviceIdStruct(
      (deviceIdElem \\ "Manufacturer").text,
      (deviceIdElem \\ "OUI").text,
      (deviceIdElem \\ "ProductClass").text,
      (deviceIdElem \\ "SerialNumber").text
    )
  }
}
