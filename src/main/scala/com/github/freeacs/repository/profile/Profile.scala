package com.github.freeacs.repository.profile

final case class Profile(
  profileId: Option[Long],
  profileName: String,
  unitTypeId: Long
)
