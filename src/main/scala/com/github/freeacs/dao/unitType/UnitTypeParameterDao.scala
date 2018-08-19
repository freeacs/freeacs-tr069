package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao
    with UnitTypeParameterTable {

  import config.profile.api._

  def readByName(
      name: String,
      unitTypeId: Long
  ): Future[Option[UnitTypeParameter]] = {
    db.run(
      unitTypeParameters
        .filter(utp => utp.name === name && utp.unitTypeId === unitTypeId)
        .result
        .headOption
    )
  }

  def readByUnitType(unitTypeId: Long): Future[Seq[UnitTypeParameter]] = {
    db.run(
      unitTypeParameters.filter(_.unitTypeId === unitTypeId).result
    )
  }

  def list(): Future[Seq[UnitTypeParameter]] =
    db.run(unitTypeParameters.result)

  def save(parameters: Seq[UnitTypeParameter]): Future[Seq[UnitTypeParameter]] =
    db.run(
      DBIO
        .sequence(
          parameters.map(
            p =>
              unitTypeParameters returning unitTypeParameters
                .map(_.unitTypeParamId)
                into (
                    (
                        unitTypeParam,
                        id
                    ) => unitTypeParam.copy(unitTypeParameterId = Some(id))
                )
                += p
          )
        )
        .transactionally
    )
}
