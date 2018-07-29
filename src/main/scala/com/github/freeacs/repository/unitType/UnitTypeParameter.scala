package com.github.freeacs.repository.unitType

final case class UnitTypeParameter(
  unitTypeParameterId: Option[Long],
  unitTypeId: Long,
  name: String,
  flags: String
)