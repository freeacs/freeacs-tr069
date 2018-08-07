package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeDao(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Dao with UnitTypeTable {

  import config.profile.api._

  def list(): Future[Seq[UnitType]] =
    db.run(unitTypes.result)

  def getByName(name: String): Future[Option[UnitType]] =
    db.run(unitTypes.filter(_.unitTypeName === name).result.headOption)

  def save(unitType: UnitType): Future[UnitType] =
    db.run(unitTypes returning unitTypes.map(_.unitTypeId)
      into ((unitType, id) => unitType.copy(unitTypeId = Some(id)))
      += unitType)
}
