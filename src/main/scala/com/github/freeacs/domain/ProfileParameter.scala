package com.github.freeacs.domain

final case class ProfileParameter(
  profileId: Long,
  unitTypeParamId: Long,
  value: Option[String]
)