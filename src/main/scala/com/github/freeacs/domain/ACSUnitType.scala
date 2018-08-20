package com.github.freeacs.domain

final case class ACSUnitType(
    unitTypeName: String,
    protocol: String,
    unitTypeId: Option[Long] = None,
    matcherId: Option[String] = None,
    vendorName: Option[String] = None,
    description: Option[String] = None,
    params: Seq[ACSUnitTypeParameter] = Seq.empty
)
