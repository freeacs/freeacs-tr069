package com.github.freeacs.session

sealed trait ExpectRequest
case object ExpectInformRequest              extends ExpectRequest
case object ExpectEmptyRequest               extends ExpectRequest
case object ExpectGetParameterNamesResponse  extends ExpectRequest
case object ExpectGetParameterValuesResponse extends ExpectRequest
case object ExpectSetParameterValuesResponse extends ExpectRequest
case object ExpectRebootResponse             extends ExpectRequest
