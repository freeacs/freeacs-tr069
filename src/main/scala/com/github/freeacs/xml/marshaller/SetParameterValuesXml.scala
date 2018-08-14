package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{
  SetParameterValuesRequest,
  SetParameterValuesResponse
}

import scala.xml.Elem

object SetParameterValuesXml
    extends XmlMarshaller[SetParameterValuesResponse, SetParameterValuesRequest]
    with EnvelopeXML {
  def marshal(informResponse: SetParameterValuesRequest): Elem =
    withEnvelope(
      <cwmp:SetParameterValues>
        <ParameterList soapenc:arrayType="cwmp:ParameterValueStruct[1]">
          <ParameterValueStruct>
            <Name>InternetGatewayDevice.ManagementServer.PeriodicInformInterval</Name>
            <Value xsi:type="xsd:unsignedInt">7000</Value>
          </ParameterValueStruct>
        </ParameterList>
      </cwmp:SetParameterValues>
    )

  def unMarshal(xml: Elem): SetParameterValuesResponse =
    SetParameterValuesResponse((xml \\ "Status").text.toInt)
}
