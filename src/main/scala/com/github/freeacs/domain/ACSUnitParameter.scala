package com.github.freeacs.domain

case class ACSUnitParameter(
    unitId: String,
    unitTypeParameter: ACSUnitTypeParameter,
    value: Option[String]
)

object ACSUnitParameter {
  type ACSUnitParameterTupleType = (String, Long, Long, Option[String])

  def toTuple(parameter: ACSUnitParameter): ACSUnitParameterTupleType =
    (
      parameter.unitId,
      parameter.unitTypeParameter.unitTypeId,
      parameter.unitTypeParameter.unitTypeParamId.get,
      parameter.value
    )

  def fromTuple(tuple: ACSUnitParameterTupleType): ACSUnitParameter =
    ACSUnitParameter(
      tuple._1,
      ACSUnitTypeParameter.fromId(tuple._3, tuple._2),
      tuple._4
    )
}
