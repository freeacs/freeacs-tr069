package com.github.freeacs.repository.unitType

import com.github.freeacs.repository.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Db with UnitTypeTable {

  import config.profile.api._

  def list(): Future[Seq[UnitType]] =
    db.run(unitTypes.result)

  def save(unitType: UnitType): Future[UnitType] =
    db.run(unitTypes returning unitTypes.map(_.unitTypeId)
      into ((unitType, id) => unitType.copy(unitTypeId = Some(id)))
      += unitType)
}
