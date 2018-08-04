package com.github.freeacs.dao.profile

final case class ProfileParameter(
  profileId: Long,
  unitTypeParamId: Long,
  value: Option[String]
)
