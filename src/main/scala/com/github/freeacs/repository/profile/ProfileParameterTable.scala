package com.github.freeacs.repository.profile

import com.github.freeacs.repository.Db
import com.github.freeacs.repository.unitType.{UnitTypeParameterTable, UnitTypeTable}

trait ProfileParameterTable extends ProfileTable with UnitTypeParameterTable { this: Db =>
  import config.profile.api._

  class ProfileParameters(tag: Tag) extends Table[ProfileParameter](tag, "profile_param") {
    def profileId = column[Long]("PROFILE_ID")
    def profileFk = foreignKey("PROFILE_FK", profileId, profiles)(
      _.profileId, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade
    )
    def unitTypeParamId = column[Long]("UNIT_TYPE_PARAM_ID")
    def unitTypeParamFk = foreignKey("UNIT_TYPE_PARAM_FK", unitTypeParamId, unitTypeParameters)(
      _.unitTypeParamId, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade
    )
    def value = column[String]("VALUE")

    def * = (profileId, unitTypeParamId, value.?) <> (ProfileParameter.tupled, ProfileParameter.unapply)
  }

  val profileParameters = TableQuery[ProfileParameters]
}