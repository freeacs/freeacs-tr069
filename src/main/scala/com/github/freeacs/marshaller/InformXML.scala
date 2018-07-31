package com.github.freeacs.marshaller

import com.github.freeacs.entities._
import EnvelopeXML._
import scala.xml.Elem

object InformXML {
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
