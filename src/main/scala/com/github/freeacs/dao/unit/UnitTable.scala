package com.github.freeacs.dao.unit

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.profile.ProfileParameterTable
import com.github.freeacs.dao.unitType.UnitTypeTable

trait UnitTable extends UnitTypeTable with ProfileParameterTable { this: Dao =>
  import config.profile.api._

  class Units(tag: Tag) extends Table[Unit](tag, "UNIT") {
    def unitId     = column[String]("UNIT_ID", O.PrimaryKey)
    def profileId  = column[Long]("PROFILE_ID")
    def unitTypeId = column[Long]("UNIT_TYPE_ID")
    def unitTypeFk = foreignKey("UNIT_TYPE_FK", unitTypeId, unitTypes)(
      _.unitTypeId,
      ForeignKeyAction.Restrict,
      ForeignKeyAction.Cascade
    )

    def * = (unitId, profileId, unitTypeId) <> (Unit.tupled, Unit.unapply)
  }

  val units = TableQuery[Units]
}
