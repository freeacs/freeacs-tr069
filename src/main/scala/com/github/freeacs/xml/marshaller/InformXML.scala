package com.github.freeacs.xml.marshaller

import com.github.freeacs.xml._
import scala.xml.Elem

object InformXML
    extends XmlMarshaller[InformRequest, InformResponse]
    with EnvelopeXML {

  def marshal(informResponse: InformResponse): Elem =
    withEnvelope(
      <cwmp:InformResponse>
        <MaxEnvelopes>{informResponse.maxEnvelopes}</MaxEnvelopes>
      </cwmp:InformResponse>
    )

  def unMarshal(xml: Elem): InformRequest =
    InformRequest(
      parseDeviceIdStruct(xml),
      parseEventStructs(xml),
      parseParameterValueStructs(xml)
    )

}
