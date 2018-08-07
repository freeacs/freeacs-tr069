package com.github.freeacs.dao.unitType

final case class UnitTypeParameter(
  name: String,
  flags: String,
  unitTypeId: Long,
  unitTypeParameterId: Option[Long] = None
)