package com.github.freeacs.repository.unit

import com.github.freeacs.repository.Db

trait UnitTable { this: Db =>
  import config.profile.api._

  class Units(tag: Tag) extends Table[Unit](tag, "unit") {
    def unitId = column[String]("UNIT_ID", O.PrimaryKey)
    def profileId = column[Long]("PROFILE_ID")
    def unitTypeId = column[Long]("UNIT_TYPE_ID")

    def * = (unitId, profileId, unitTypeId) <> (Unit.tupled, Unit.unapply)
  }

  val units = TableQuery[Units]
}