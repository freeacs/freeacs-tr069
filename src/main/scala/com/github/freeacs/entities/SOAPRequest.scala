package com.github.freeacs.entities

sealed trait SOAPRequest

final case class InformRequest(params: Seq[ParameterValueStruct]) extends SOAPRequest
final case class UnknownRequest(method: SOAPMethod.Value) extends SOAPRequest