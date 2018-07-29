package com.github.freeacs.repository.unit

final case class UnitParameter(
  unitId: String,
  unitTypeParameterId: Long,
  value: Option[String]
)
