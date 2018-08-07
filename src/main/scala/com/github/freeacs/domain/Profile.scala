package com.github.freeacs.domain

case class Profile(
  profileName: String,
  unitTypeId: Long,
  profileId: Option[Long],
)
