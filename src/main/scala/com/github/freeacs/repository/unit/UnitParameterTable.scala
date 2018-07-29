package com.github.freeacs.repository.unit

import com.github.freeacs.repository.Db

trait UnitParameterTable { this: Db =>
  import config.profile.api._

  class UnitParameters(tag: Tag) extends Table[UnitParameter](tag, "unit_param") {
    def unitId = column[String]("UNIT_ID")
    def unitTypeId = column[Long]("UNIT_TYPE_PARAM_ ID")
    def value = column[String]("VALUE")

    def * = (unitId, unitTypeId, value.?) <> (UnitParameter.tupled, UnitParameter.unapply)
  }

  val unitParameters = TableQuery[UnitParameters]
}