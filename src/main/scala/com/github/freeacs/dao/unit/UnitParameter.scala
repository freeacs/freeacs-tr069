package com.github.freeacs.dao.unit

final case class UnitParameter(
    unitId: String,
    unitTypeParameterId: Long,
    value: Option[String]
)
