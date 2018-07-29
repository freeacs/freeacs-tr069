package com.github.freeacs.repository.unitType

import com.github.freeacs.repository.Db

trait UnitTypeParameterTable extends UnitTypeTable { this: Db =>
  import config.profile.api._

  class UnitTypeParameters(tag: Tag) extends Table[UnitTypeParameter](tag, "unit_type_param") {
    def unitTypeParamId = column[Long]("UNIT_TYPE_PARAM_ID", O.PrimaryKey, O.AutoInc)
    def unitTypeId = column[Long]("UNIT_TYPE_ID")
    def name = column[String]("NAME")
    def flags = column[String]("FLAGS")

    def * = (unitTypeParamId.?, unitTypeId, name, flags) <> (UnitTypeParameter.tupled, UnitTypeParameter.unapply)
  }

  val unitTypeParameters = TableQuery[UnitTypeParameters]
}