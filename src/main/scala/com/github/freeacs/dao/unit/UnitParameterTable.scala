package com.github.freeacs.dao.unit

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.unitType.UnitTypeParameterTable

trait UnitParameterTable extends UnitTypeParameterTable { this: Dao =>
  import config.profile.api._

  class UnitParameters(tag: Tag)
      extends Table[UnitParameter](tag, "UNIT_PARAM") {
    def unitId          = column[String]("UNIT_ID")
    def unitTypeParamId = column[Long]("UNIT_TYPE_PARAM_ID")
    def unitTypeParamFk =
      foreignKey("UNIT_TYPE_PARAM_FK", unitTypeParamId, unitTypeParameters)(
        _.unitTypeParamId,
        ForeignKeyAction.Restrict,
        ForeignKeyAction.Cascade
      )

    def value = column[String]("VALUE")

    def pk = primaryKey("pk_a", (unitId, unitTypeParamId))

    def * =
      (unitId, unitTypeParamId, value.?) <> (UnitParameter.tupled, UnitParameter.unapply)
  }

  val unitParameters = TableQuery[UnitParameters]
}
