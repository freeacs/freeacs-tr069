package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.unitType.UnitTypeTable

trait ProfileTable extends UnitTypeTable { this: Dao =>
  import config.profile.api._

  class Profiles(tag: Tag) extends Table[Profile](tag, "PROFILE") {
    def profileId   = column[Long]("PROFILE_ID", O.PrimaryKey, O.AutoInc)
    def profileName = column[String]("PROFILE_NAME")
    def unitTypeId  = column[Long]("UNIT_TYPE_ID")
    def unitTypeFk = foreignKey("UNIT_TYPE_FK", unitTypeId, unitTypes)(
      _.unitTypeId,
      ForeignKeyAction.Restrict,
      ForeignKeyAction.Cascade
    )

    def * =
      (profileName, unitTypeId, profileId.?) <> (Profile.tupled, Profile.unapply)
  }

  val profiles = TableQuery[Profiles]
}
