package com.github.freeacs.repository.profile

import com.github.freeacs.repository.Db

trait ProfileTable { this: Db =>
  import config.profile.api._

  class Profiles(tag: Tag) extends Table[Profile](tag, "profile") {
    def profileId = column[Long]("PROFILE_ID", O.PrimaryKey, O.AutoInc)
    def profileName = column[String]("PROFILE_NAME")
    def unitTypeId = column[Long]("UNIT_TYPE_ID")

    def * = (profileId.?, profileName, unitTypeId) <> (Profile.tupled, Profile.unapply)
  }

  val profiles = TableQuery[Profiles]
}