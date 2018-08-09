package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{
  GetParameterValuesRequest,
  GetParameterValuesResponse
}

import scala.xml.{Elem, NodeSeq}

object GetParameterValuesXml
    extends XmlMarshaller[GetParameterValuesResponse, GetParameterValuesRequest]
    with EnvelopeXML {

  def marshal(gpv: GetParameterValuesRequest): Elem =
    withEnvelope(
      <cwmp:GetParameterValues>
        <ParameterNames soapenc:arrayType={s"xsd:string[${gpv.params.size}]"}>
          {gpv.params.map(param => <string>{param}</string>)}
        </ParameterNames>
      </cwmp:GetParameterValues>
    )

  def unMarshal(xml: Elem): GetParameterValuesResponse =
    GetParameterValuesResponse(
      (xml \\ "ParameterValueStruct").seq.map(struct => {
        val name  = (struct \\ "Name").text
        val value = (struct \\ "Value").text
        (name, value)
      })
    )
}
