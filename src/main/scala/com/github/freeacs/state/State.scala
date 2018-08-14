package com.github.freeacs.state
import com.github.freeacs.xml.SOAPResponse

sealed trait State {
  val response: SOAPResponse
}

case class ExpectInformRequest(response: SOAPResponse) extends State

case class ExpectEmptyRequest(response: SOAPResponse) extends State

case class ExpectGetParameterNamesResponse(response: SOAPResponse) extends State

case class ExpectGetParameterValuesResponse(response: SOAPResponse)
    extends State

case class ExpectSetParameterValuesResponse(response: SOAPResponse)
    extends State

case class ExpectRebootResponse(response: SOAPResponse) extends State

case class Complete(response: SOAPResponse) extends State

case class Failed(response: SOAPResponse) extends State
