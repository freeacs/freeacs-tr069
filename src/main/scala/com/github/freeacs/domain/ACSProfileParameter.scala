package com.github.freeacs.domain

final case class ACSProfileParameter(
    profileId: Long,
    unitTypeParamId: Long,
    value: Option[String]
)
