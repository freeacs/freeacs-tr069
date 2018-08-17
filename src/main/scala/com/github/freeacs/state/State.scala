package com.github.freeacs.state

sealed trait State

case object ExpectInformRequest extends State

case object ExpectEmptyRequest extends State

case object ExpectGetParameterNamesResponse extends State

case object ExpectGetParameterValuesResponse extends State

case object ExpectSetParameterValuesResponse extends State

case object ExpectRebootResponse extends State

case object Complete extends State

case object Failed extends State
