package com.github.freeacs.domain

case class ACSUnit(
    unitId: String,
    unitType: ACSUnitType,
    profile: ACSProfile,
    params: Seq[ACSUnitParameter] = Seq.empty
)

object ACSUnit {
  type ACSUnitTupleType = (String, Long, Long)

  def toTuple(unit: ACSUnit): ACSUnitTupleType =
    (unit.unitId, unit.unitType.unitTypeId.get, unit.profile.profileId.get)

  def fromTuple(tuple: ACSUnitTupleType): ACSUnit =
    ACSUnit(
      tuple._1,
      ACSUnitType.fromId(tuple._2),
      ACSProfile.fromId(tuple._3, tuple._2)
    )
}
