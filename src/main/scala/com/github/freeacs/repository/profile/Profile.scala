package com.github.freeacs.repository.profile

final case class Profile(
  profileId: Option[Long] = None,
  profileName: String,
  unitTypeId: Long
)
