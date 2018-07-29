package com.github.freeacs.repository.unitType

final case class UnitType(
  unitTypeId: Option[Long],
  unitTypeName: String,
  matcherId: Option[String],
  vendorName: Option[String],
  description: Option[String],
  protocol: String
)