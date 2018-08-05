package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{GetParameterNamesRequest, GetParameterNamesResponse}

import scala.xml.{Elem, NodeSeq}

object GetParameterNamesXml {

  def marshal(gpn: GetParameterNamesRequest): NodeSeq =
    <soap:Envelope
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
    xmlns:cwmp="urn:dslforum-org:cwmp-1-1">
      <soap:Header>
      </soap:Header>
      <soap:Body>
        <cwmp:GetParameterNames>
          <ParameterPath>
            {gpn.param}
          </ParameterPath>
          <NextLevel>false</NextLevel>
        </cwmp:GetParameterNames>
      </soap:Body>
    </soap:Envelope>

  def unMarshal(xml: Elem): GetParameterNamesResponse =
    GetParameterNamesResponse(
      (xml \\ "ParameterInfoStruct").seq.map(struct => {
        val name = (struct \\ "Name").text
        val writableStr = (struct \\ "Writable").text
        (name, writableStr != "0")
      })
    )
}
