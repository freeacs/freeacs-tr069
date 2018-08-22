package com.github.freeacs.domain
import com.github.freeacs.domain.ACSUnit.UnitId.UnitId
import com.github.freeacs.domain.ACSUnitParameter.UnitParameterValue.UnitParameterValue
import shapeless.tag.@@

case class ACSUnitParameter(
    unitId: UnitId,
    unitTypeParameter: ACSUnitTypeParameter,
    value: Option[UnitParameterValue]
)

object ACSUnitParameter {

  type ACSUnitParameterTupleType =
    (String, Long, Option[String])

  def toTuple(parameter: ACSUnitParameter): ACSUnitParameterTupleType =
    (
      parameter.unitId.toString,
      parameter.unitTypeParameter.unitTypeParamId.get.toLong,
      parameter.value.map(_.toString)
    )

  object UnitParameterValue {
    trait Tag
    type UnitParameterValue = String @@ Tag

    def apply(v: String): UnitParameterValue =
      shapeless.tag[Tag][String](v)
  }
}
