package com.github.freeacs.xml

object SOAPMethod extends Enumeration {
  type Method = Value
  val Inform, GetParameterNamesResponse, GetParameterValuesResponse, SetParameterValues, Empty = Value

}
