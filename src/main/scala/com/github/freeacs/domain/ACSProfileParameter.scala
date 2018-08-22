package com.github.freeacs.domain

import com.github.freeacs.domain.ACSProfile.ProfileId
import com.github.freeacs.domain.ACSProfile.ProfileId.ProfileId
import com.github.freeacs.domain.ACSProfileParameter.ProfileParameterValue.ProfileParameterValue
import com.github.freeacs.domain.ACSUnitTypeParameter.UnitTypeParameterId
import com.github.freeacs.domain.ACSUnitTypeParameter.UnitTypeParameterId.UnitTypeParameterId
import shapeless.tag.@@

final case class ACSProfileParameter(
    profileId: ProfileId,
    unitTypeParamId: UnitTypeParameterId,
    value: Option[ProfileParameterValue]
)

object ACSProfileParameter {
  type ACSProfileParameterTupleType =
    (Long, Long, Option[String])

  def fromResultSet(
      profileId: Long,
      unitTypeParameterId: Long,
      value: Option[String]
  ): ACSProfileParameter =
    ACSProfileParameter(
      ProfileId(profileId),
      UnitTypeParameterId(unitTypeParameterId),
      value.map(ProfileParameterValue.apply)
    )

  object ProfileParameterValue {
    trait Tag
    type ProfileParameterValue = String @@ Tag

    def apply(v: String): ProfileParameterValue =
      shapeless.tag[Tag][String](v)
  }
}
