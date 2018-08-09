package com.github.freeacs.dao.profile

final case class Profile(
    profileName: String,
    unitTypeId: Long,
    profileId: Option[Long] = None
)
