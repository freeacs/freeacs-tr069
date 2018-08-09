package com.github.freeacs.domain

case class UnitTypeParameter(
    name: String,
    flags: String,
    unitTypeId: Long,
    unitTypeParamId: Option[Long]
)
