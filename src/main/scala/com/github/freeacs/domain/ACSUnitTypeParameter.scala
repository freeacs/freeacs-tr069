package com.github.freeacs.domain

case class ACSUnitTypeParameter(
    name: String,
    flags: String,
    unitTypeId: Long,
    unitTypeParamId: Option[Long]
)

object ACSUnitTypeParameter {
  type ACSUnitTypeParameterTupleType = (String, String, Long, Option[Long])

  def toTuple(parameter: ACSUnitTypeParameter): ACSUnitTypeParameterTupleType =
    (
      parameter.name,
      parameter.flags,
      parameter.unitTypeId,
      parameter.unitTypeParamId
    )

  def fromTuple(
      tupleType: ACSUnitTypeParameterTupleType
  ): ACSUnitTypeParameter =
    ACSUnitTypeParameter(tupleType._1, tupleType._2, tupleType._3, tupleType._4)

  def fromId(id: Long, unitTypeId: Long): ACSUnitTypeParameter =
    ACSUnitTypeParameter(
      name = "",
      flags = "",
      unitTypeId = unitTypeId,
      unitTypeParamId = Some(id)
    )
}
