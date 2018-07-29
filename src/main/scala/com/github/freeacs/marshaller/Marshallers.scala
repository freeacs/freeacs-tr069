package com.github.freeacs.marshaller

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.marshalling.{Marshal, Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.github.freeacs.entities._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.xml.{Elem, NodeSeq, XML}

trait Marshallers extends ScalaXmlSupport {

  def withEnvelope(xml: Elem) =
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                      xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
                      xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns:cwmp="urn:dslforum-org:cwmp-1-0">
      <soapenv:Header>
        <cwmp:ID soapenv:mustUnderstand="1">1</cwmp:ID>
      </soapenv:Header>
      {xml}
    </soapenv:Envelope>

  def withBody(xml: Elem) =
    <soapenv:Body>
      {xml}
    </soapenv:Body>

  def marshalInformResponseXml(informResponse: InformResponse) =
    withEnvelope(withBody(
      <cwmp:InformResponse>
        <MaxEnvelopes>{informResponse.maxEnvelopes}</MaxEnvelopes>
      </cwmp:InformResponse>
    ))

  implicit def informResponseXmlFormat =
    Marshaller.opaque[InformResponse, NodeSeq](marshalInformResponseXml)

  implicit def informResponseMarshaller(implicit ec: ExecutionContext): ToResponseMarshaller[InformResponse] =
    Marshaller.oneOf(
      Marshaller.withOpenCharset(MediaTypes.`text/xml`) { (inR, charset) =>
        HttpResponse(entity =
          HttpEntity.CloseDelimited(
            ContentType.WithCharset(MediaTypes.`text/xml`, HttpCharsets.`UTF-8`),
            Source.fromFuture(Marshal(inR).to[NodeSeq])
              .map(ns => ByteString(ns.toString))
          ))
      }
    )

  implicit def soapRequestXmlUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[SOAPRequest] =
    Unmarshaller.byteStringUnmarshaller
      .mapWithCharset { (data, charset) =>
        if (data.nonEmpty)
          parseSOAPRequest(XML.loadString(decodeData(data, charset)))
        else
          UnknownRequest(SOAPMethod.Empty)
      }

  def parseSOAPRequest(xml: Elem): SOAPRequest = {
    parseMethod(xml) match {
      case SOAPMethod.Inform =>
        InformRequest(parseParameterValueStructs(xml))
      case unknown =>
        UnknownRequest(unknown)
    }
  }

  def parseMethod(xml: Elem): SOAPMethod.Value =
    (xml \\ "Body").headOption.flatMap(_.child.collectFirst {
      case el: Elem => Try(SOAPMethod.withName(el.label)).getOrElse(SOAPMethod.Unknown)
    }) getOrElse SOAPMethod.Empty

  def parseParameterValueStructs(xml: Elem): immutable.Seq[ParameterValueStruct] =
    (xml \\ "ParameterList" \\ "ParameterValueStruct").seq
      .map(p => ParameterValueStruct((p \\ "Name").text, (p \\ "Value").text))

  def decodeData(data: ByteString, charset: HttpCharset): String =
    if (charset == HttpCharsets.`UTF-8`)
      data.utf8String
    else
      data.decodeString(charset.nioCharset.name)
}
