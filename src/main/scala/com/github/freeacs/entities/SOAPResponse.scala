package com.github.freeacs.entities

sealed trait SOAPResponse

final case class InformResponse(maxEnvelopes: Int = 1) extends SOAPResponse
object EmptyResponse extends SOAPResponse