package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}

import scala.xml.Elem

trait XmlMarshaller[IN <: SOAPRequest, OUT <: SOAPResponse] {
  def marshal(informResponse: OUT): Elem

  def unMarshal(xml: Elem): IN
}
