package com.github.freeacs.domain

case class ACSUnitParameter(
    unitId: String,
    unitTypeParameter: ACSUnitTypeParameter,
    value: Option[String]
)
