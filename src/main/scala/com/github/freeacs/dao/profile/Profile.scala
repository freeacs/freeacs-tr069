package com.github.freeacs.dao.profile

final case class Profile(
  profileId: Option[Long] = None,
  profileName: String,
  unitTypeId: Long
)
