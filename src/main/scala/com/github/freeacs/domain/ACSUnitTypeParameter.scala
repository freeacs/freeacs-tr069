package com.github.freeacs.domain
import com.github.freeacs.domain.ACSUnitType.UnitTypeId
import com.github.freeacs.domain.ACSUnitType.UnitTypeId.UnitTypeId
import com.github.freeacs.domain.ACSUnitTypeParameter.UnitTypeParameterFlags.UnitTypeParameterFlags
import com.github.freeacs.domain.ACSUnitTypeParameter.UnitTypeParameterId.UnitTypeParameterId
import com.github.freeacs.domain.ACSUnitTypeParameter.UnitTypeParameterName.UnitTypeParameterName
import shapeless.tag.@@

case class ACSUnitTypeParameter(
    name: UnitTypeParameterName,
    flags: UnitTypeParameterFlags,
    unitTypeId: UnitTypeId,
    unitTypeParamId: Option[UnitTypeParameterId]
)

object ACSUnitTypeParameter {

  type ACSUnitTypeParameterTupleType = (String, String, Long, Option[Long])

  def toTuple(parameter: ACSUnitTypeParameter): ACSUnitTypeParameterTupleType =
    (
      parameter.name.toString,
      parameter.flags.toString,
      parameter.unitTypeId.toLong,
      parameter.unitTypeParamId.map(_.toLong)
    )

  def fromResultSet(
      name: String,
      flags: String,
      unitTypeId: Long,
      unitTypeParamId: Option[Long]
  ): ACSUnitTypeParameter =
    ACSUnitTypeParameter(
      name = UnitTypeParameterName(name),
      flags = UnitTypeParameterFlags(flags),
      unitTypeId = UnitTypeId(unitTypeId),
      unitTypeParamId = unitTypeParamId.map(UnitTypeParameterId.apply)
    )

  object UnitTypeParameterId {
    trait Tag
    type UnitTypeParameterId = Long @@ Tag

    def apply(v: Long): UnitTypeParameterId =
      shapeless.tag[Tag][Long](v)
  }

  object UnitTypeParameterName {
    trait Tag
    type UnitTypeParameterName = String @@ Tag

    def apply(v: String): UnitTypeParameterName =
      shapeless.tag[Tag][String](v)
  }

  object UnitTypeParameterFlags {
    trait Tag
    type UnitTypeParameterFlags = String @@ Tag

    def apply(v: String): UnitTypeParameterFlags =
      shapeless.tag[Tag][String](v)
  }

}
