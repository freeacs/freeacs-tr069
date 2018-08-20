package com.github.freeacs.domain

case class ACSProfile(
    profileName: String,
    unitTypeId: Long,
    profileId: Option[Long],
)
