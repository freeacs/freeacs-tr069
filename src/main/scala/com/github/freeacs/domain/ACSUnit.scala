package com.github.freeacs.domain
import com.github.freeacs.domain.ACSUnit.UnitId.UnitId
import shapeless.tag.@@

case class ACSUnit(
    unitId: UnitId,
    unitType: ACSUnitType,
    profile: ACSProfile,
    params: Seq[ACSUnitParameter] = Seq.empty
)

object ACSUnit {
  object UnitId {
    trait Tag
    type UnitId = String @@ Tag

    def apply(v: String): UnitId =
      shapeless.tag[Tag][String](v)
  }
}
