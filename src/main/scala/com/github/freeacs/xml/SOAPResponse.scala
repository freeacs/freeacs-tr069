package com.github.freeacs.xml

sealed trait SOAPResponse

final case class InformResponse(maxEnvelopes: Int = 1) extends SOAPResponse

case object InvalidRequest extends SOAPResponse