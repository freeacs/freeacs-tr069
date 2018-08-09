package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao

trait UnitTypeTable { this: Dao =>
  import config.profile.api._

  class UnitTypes(tag: Tag) extends Table[UnitType](tag, "UNIT_TYPE") {
    def unitTypeId   = column[Long]("UNIT_TYPE_ID", O.PrimaryKey, O.AutoInc)
    def unitTypeName = column[String]("UNIT_TYPE_NAME")
    def matcherId    = column[String]("MATCHER_ID")
    def vendorName   = column[String]("VENDOR_NAME")
    def description  = column[String]("DESCRIPTION")
    def protocol     = column[String]("PROTOCOL")

    def * =
      (
        unitTypeName,
        protocol,
        unitTypeId.?,
        matcherId.?,
        vendorName.?,
        description.?
      ) <> (UnitType.tupled, UnitType.unapply)
  }

  val unitTypes = TableQuery[UnitTypes]
}
