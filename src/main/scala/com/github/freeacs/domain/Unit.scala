package com.github.freeacs.domain

case class Unit(
    unitId: String,
    unitType: UnitType,
    profile: Profile,
    params: Seq[UnitParameter] = Seq.empty
)
