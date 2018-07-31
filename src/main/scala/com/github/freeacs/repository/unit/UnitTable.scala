package com.github.freeacs.repository.unit

import com.github.freeacs.repository.Database
import com.github.freeacs.repository.profile.ProfileParameterTable
import com.github.freeacs.repository.unitType.UnitTypeTable

trait UnitTable extends UnitTypeTable with ProfileParameterTable { this: Database =>
  import config.profile.api._

  class Units(tag: Tag) extends Table[Unit](tag, "UNIT") {
    def unitId = column[String]("UNIT_ID", O.PrimaryKey)
    def profileId = column[Long]("PROFILE_ID")
    def profileFK = foreignKey("PROFILE_FK", profileId, profiles)(
      _.profileId, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade
    )
    def unitTypeId = column[Long]("UNIT_TYPE_ID")
    def unitTypeFk = foreignKey("UNIT_TYPE_FK", unitTypeId, unitTypes)(
      _.unitTypeId, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade
    )

    def * = (unitId, profileId, unitTypeId) <> (Unit.tupled, Unit.unapply)
  }

  val units = TableQuery[Units]
}