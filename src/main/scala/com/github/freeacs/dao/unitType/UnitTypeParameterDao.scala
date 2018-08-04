package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class UnitTypeParameterDao(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Dao with UnitTypeParameterTable {

  import config.profile.api._

  def list(): Future[Seq[UnitTypeParameter]] =
    db.run(unitTypeParameters.result)

  def save(parameters: immutable.Iterable[UnitTypeParameter]): Future[Iterable[UnitTypeParameter]] =
    db.run(DBIO.sequence(parameters.map(p => unitTypeParameters returning unitTypeParameters.map(_.unitTypeParamId)
      into ((unitTypeParam, id) => unitTypeParam.copy(unitTypeParameterId = Some(id)))
      += p)).transactionally)
}
