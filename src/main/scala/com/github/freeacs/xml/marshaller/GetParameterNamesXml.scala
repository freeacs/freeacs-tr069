package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{
  GetParameterNamesRequest,
  GetParameterNamesResponse
}

import scala.xml.{Elem, NodeSeq}

object GetParameterNamesXml
    extends XmlMarshaller[GetParameterNamesResponse, GetParameterNamesRequest]
    with EnvelopeXML {

  def marshal(gpn: GetParameterNamesRequest): Elem =
    withEnvelope(
      <cwmp:GetParameterNames>
        <ParameterPath>
          {gpn.param}
        </ParameterPath>
        <NextLevel>false</NextLevel>
      </cwmp:GetParameterNames>
    )

  def unMarshal(xml: Elem): GetParameterNamesResponse =
    GetParameterNamesResponse(
      (xml \\ "ParameterInfoStruct").seq.map(struct => {
        val name        = (struct \\ "Name").text
        val writableStr = (struct \\ "Writable").text
        (name, writableStr != "0")
      })
    )
}
