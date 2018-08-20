package com.github.freeacs.domain

case class ACSUnit(
    unitId: String,
    unitType: ACSUnitType,
    profile: ACSProfile,
    params: Seq[ACSUnitParameter] = Seq.empty
)
