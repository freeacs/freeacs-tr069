package com.github.freeacs.repository.unit

import com.github.freeacs.repository.Db
import com.github.freeacs.repository.unitType.UnitTypeParameterTable

trait UnitParameterTable extends UnitTypeParameterTable { this: Db =>
  import config.profile.api._

  class UnitParameters(tag: Tag) extends Table[UnitParameter](tag, "unit_param") {
    def unitId = column[String]("UNIT_ID")
    def unitTypeParamId = column[Long]("UNIT_TYPE_PARAM_ID")
    def unitTypeParamFk = foreignKey("UNIT_TYPE_PARAM_FK", unitTypeParamId, unitTypeParameters)(
      _.unitTypeParamId, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade
    )

    def value = column[String]("VALUE")

    def * = (unitId, unitTypeParamId, value.?) <> (UnitParameter.tupled, UnitParameter.unapply)
  }

  val unitParameters = TableQuery[UnitParameters]
}