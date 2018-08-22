package com.github.freeacs.domain

import com.github.freeacs.domain.ACSProfile.ProfileId.ProfileId
import com.github.freeacs.domain.ACSProfile.ProfileName.ProfileName
import com.github.freeacs.domain.ACSUnitType.UnitTypeId
import com.github.freeacs.domain.ACSUnitType.UnitTypeId.UnitTypeId
import shapeless.tag.@@

case class ACSProfile(
    profileName: ProfileName,
    unitTypeId: UnitTypeId,
    profileId: Option[ProfileId],
)

object ACSProfile {

  type ACSProfileTupleType = (ProfileName, UnitTypeId, Option[ProfileId])

  def fromResultSet(
      profileName: String,
      unitTypeId: Long,
      profileId: Option[Long]
  ): ACSProfile =
    ACSProfile(
      ProfileName(profileName),
      UnitTypeId(unitTypeId),
      profileId.map(ProfileId.apply)
    )

  object ProfileId {
    trait Tag
    type ProfileId = Long @@ Tag

    def apply(v: Long): ProfileId =
      shapeless.tag[Tag][Long](v)
  }

  object ProfileName {
    trait Tag
    type ProfileName = String @@ Tag

    def apply(v: String): ProfileName =
      shapeless.tag[Tag][String](v)
  }
}
