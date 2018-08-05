package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{SetParameterValuesRequest, SetParameterValuesResponse}

import scala.xml.Elem

object SetParameterValuesXml extends XmlMarshaller[SetParameterValuesResponse, SetParameterValuesRequest] {
  def marshal(informResponse: SetParameterValuesRequest): Elem = ???

  def unMarshal(xml: Elem): SetParameterValuesResponse = ???
}
