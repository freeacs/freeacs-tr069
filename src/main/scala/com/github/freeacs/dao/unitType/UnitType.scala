package com.github.freeacs.dao.unitType

final case class UnitType(
    unitTypeName: String,
    protocol: String,
    unitTypeId: Option[Long] = None,
    description: Option[String] = None,
    matcherId: Option[String] = None,
    vendorName: Option[String] = None
)
