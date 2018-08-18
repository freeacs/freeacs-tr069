package com.github.freeacs.xml

sealed trait SOAPResponse

case class InformResponse(maxEnvelopes: Int = 1) extends SOAPResponse

case class InvalidRequest() extends SOAPResponse

case class EmptyResponse() extends SOAPResponse

case class GetParameterNamesRequest(param: String) extends SOAPResponse

case class GetParameterValuesRequest(params: Seq[String]) extends SOAPResponse

case class SetParameterValuesRequest() extends SOAPResponse
