package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.unitType.{UnitTypeParameterTable, UnitTypeTable}

trait ProfileParameterTable extends ProfileTable with UnitTypeParameterTable {
  this: Dao =>
  import config.profile.api._

  class ProfileParameters(tag: Tag)
      extends Table[ProfileParameter](tag, "PROFILE_PARAM") {
    def profileId = column[Long]("PROFILE_ID")
    def profileFk = foreignKey("PROFILE_FK", profileId, profiles)(
      _.profileId,
      ForeignKeyAction.Restrict,
      ForeignKeyAction.Cascade
    )
    def unitTypeParamId = column[Long]("UNIT_TYPE_PARAM_ID")
    def unitTypeParamFk =
      foreignKey("UNIT_TYPE_PARAM_FK", unitTypeParamId, unitTypeParameters)(
        _.unitTypeParamId,
        ForeignKeyAction.Restrict,
        ForeignKeyAction.Cascade
      )
    def value = column[String]("VALUE")

    def profilePK = primaryKey("PROFILE_PK", (profileId, unitTypeParamId))

    def * =
      (profileId, unitTypeParamId, value.?) <> (ProfileParameter.tupled, ProfileParameter.unapply)
  }

  val profileParameters = TableQuery[ProfileParameters]
}
