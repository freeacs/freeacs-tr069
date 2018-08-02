package com.github.freeacs.xml.marshaller

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.marshalling.{Marshal, Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.github.freeacs.xml._

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.xml.{Elem, NodeSeq, XML}

trait Marshallers extends ScalaXmlSupport {

  implicit def soapResponseXmlFormat =
    Marshaller.opaque[SOAPResponse, NodeSeq] {
      case inform: InformResponse =>
        InformXML.marshal(inform)
    }

  implicit def soapResponseXmlMarshaller(implicit ec: ExecutionContext): ToResponseMarshaller[SOAPResponse] =
    Marshaller.oneOf(
      Marshaller.withOpenCharset(MediaTypes.`text/xml`) { (response, charset) =>
        HttpResponse(entity =
          HttpEntity.CloseDelimited(
            ContentType.WithCharset(MediaTypes.`text/xml`, HttpCharsets.`UTF-8`),
            Source.fromFuture(Marshal(response).to[NodeSeq])
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
          EmptyRequest
      }

  def parseSOAPRequest(xml: Elem): SOAPRequest =
    parseMethod(xml) match {
      case SOAPMethod.Inform =>
        InformXML.unMarshal(xml)
      case unknown =>
        EmptyRequest
    }

  def parseMethod(xml: Elem): SOAPMethod.Value =
    (xml \\ "Body").headOption.flatMap(_.child.collectFirst {
      case el: Elem => Try(SOAPMethod.withName(el.label)).getOrElse(SOAPMethod.Empty)
    }) getOrElse SOAPMethod.Empty

  def decodeData(data: ByteString, charset: HttpCharset): String =
    if (charset == HttpCharsets.`UTF-8`)
      data.utf8String
    else
      data.decodeString(charset.nioCharset.name)
}
