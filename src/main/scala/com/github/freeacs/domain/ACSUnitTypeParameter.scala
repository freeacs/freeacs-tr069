package com.github.freeacs.domain

case class ACSUnitTypeParameter(
    name: String,
    flags: String,
    unitTypeId: Long,
    unitTypeParamId: Option[Long]
)
