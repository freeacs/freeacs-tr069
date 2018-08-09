package com.github.freeacs.xml.marshaller

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.util.ByteString
import com.github.freeacs.xml._

import scala.xml.{Elem, NodeSeq, XML}

object Marshallers extends ScalaXmlSupport {

  implicit def soapResponseXmlFormat
    : Marshaller[SOAPResponse, Either[SOAPResponse, NodeSeq]] =
    Marshaller.opaque[SOAPResponse, Either[SOAPResponse, NodeSeq]] {
      case inform: InformResponse =>
        Right(InformXML.marshal(inform))
      case gpn: GetParameterNamesRequest =>
        Right(GetParameterNamesXml.marshal(gpn))
      case gpv: GetParameterValuesRequest =>
        Right(GetParameterValuesXml.marshal(gpv))
      case spv: SetParameterValuesRequest =>
        Right(SetParameterValuesXml.marshal(spv))
      case response @ (InvalidRequest | EmptyResponse) =>
        Left(response)
    }

  implicit def soapRequestXmlUnmarshaller(
      implicit mat: Materializer): FromEntityUnmarshaller[SOAPRequest] =
    Unmarshaller.byteStringUnmarshaller
      .mapWithCharset { (data, charset) =>
        if (data.nonEmpty)
          parseSOAPRequest(XML.loadString(decodeData(data, charset)))
        else
          EmptyRequest
      }

  def parseSOAPRequest(xml: Elem): SOAPRequest =
    parseMethod(xml) match {
      case SOAPMethod.Inform =>
        InformXML.unMarshal(xml)
      case SOAPMethod.GetParameterNamesResponse =>
        GetParameterNamesXml.unMarshal(xml)
      case SOAPMethod.GetParameterValuesResponse =>
        GetParameterValuesXml.unMarshal(xml)
      case SOAPMethod.SetParameterValuesResponse =>
        SetParameterValuesXml.unMarshal(xml)
      case SOAPMethod.Empty =>
        EmptyRequest
    }

  def parseMethod(xml: Elem): SOAPMethod.Value =
    (xml \\ "Body").headOption
      .flatMap(_.child.collectFirst {
        case el: Elem => SOAPMethod.values.find(_.toString == el.label)
      })
      .flatten getOrElse SOAPMethod.Empty

  def decodeData(data: ByteString, charset: HttpCharset): String =
    if (charset == HttpCharsets.`UTF-8`)
      data.utf8String
    else
      data.decodeString(charset.nioCharset.name)
}
