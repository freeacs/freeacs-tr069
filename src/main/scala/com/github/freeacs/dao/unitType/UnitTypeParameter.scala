package com.github.freeacs.dao.unitType

final case class UnitTypeParameter(
  unitTypeParameterId: Option[Long] = None,
  unitTypeId: Long,
  name: String,
  flags: String
)