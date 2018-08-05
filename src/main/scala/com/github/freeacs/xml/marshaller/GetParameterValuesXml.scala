package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{GetParameterValuesRequest, GetParameterValuesResponse}

import scala.xml.{Elem, NodeSeq}

object GetParameterValuesXml {
  def marshal(gpv: GetParameterValuesRequest): NodeSeq =
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                      xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cwmp="urn:dslforum-org:cwmp-1-0">
      <soapenv:Header>
        <cwmp:ID soapenv:mustUnderstand="1">FREEACS-0</cwmp:ID>
      </soapenv:Header>
      <soapenv:Body>
        <cwmp:GetParameterValues>
          <ParameterNames soapenc:arrayType={s"xsd:string[${gpv.params.size}]"}>
            {gpv.params.map(param => <string>{param}</string>)}
          </ParameterNames>
        </cwmp:GetParameterValues>
      </soapenv:Body>
    </soapenv:Envelope>

  def unMarshal(xml: Elem): GetParameterValuesResponse =
    GetParameterValuesResponse(
      (xml \\ "ParameterValueStruct").seq.map(struct => {
        val name = (struct \\ "Name").text
        val value = (struct \\ "Value").text
        (name, value)
      })
    )
}
