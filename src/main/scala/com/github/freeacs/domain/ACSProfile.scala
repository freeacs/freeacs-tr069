package com.github.freeacs.domain

case class ACSProfile(
    profileName: String,
    unitTypeId: Long,
    profileId: Option[Long],
)

object ACSProfile {
  type ACSProfileTupleType = (String, Long, Option[Long])

  def toTuple(profile: ACSProfile): ACSProfileTupleType =
    (profile.profileName, profile.unitTypeId, profile.profileId)

  def fromTuple(tuple: ACSProfileTupleType): ACSProfile =
    ACSProfile(tuple._1, tuple._2, tuple._3)

  def fromId(id: Long, unitTypeId: Long): ACSProfile =
    ACSProfile(
      profileName = "",
      unitTypeId = unitTypeId,
      profileId = Some(id)
    )
}
