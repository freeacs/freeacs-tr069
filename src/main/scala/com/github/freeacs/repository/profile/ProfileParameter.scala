package com.github.freeacs.repository.profile

final case class ProfileParameter(
  profileId: Long,
  unitTypeParamId: Long,
  value: Option[String]
)
