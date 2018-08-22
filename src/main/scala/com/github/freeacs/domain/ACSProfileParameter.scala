package com.github.freeacs.domain

final case class ACSProfileParameter(
    profileId: Long,
    unitTypeParamId: Long,
    value: Option[String]
)

object ACSProfileParameter {
  type ACSProfileParameterTupleType = (Long, Long, Option[String])

  def toTuple(parameter: ACSProfileParameter): ACSProfileParameterTupleType =
    (parameter.profileId, parameter.unitTypeParamId, parameter.value)

  def fromTuple(tuple: ACSProfileParameterTupleType): ACSProfileParameter = {
    ACSProfileParameter(tuple._1, tuple._2, tuple._3)
  }
}
