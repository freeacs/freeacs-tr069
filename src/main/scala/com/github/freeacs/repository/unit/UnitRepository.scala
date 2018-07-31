package com.github.freeacs.repository.unit

import com.github.freeacs.repository.Database
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Database with UnitTable {

  import config.profile.api._

  def list(): Future[Seq[Unit]] =
  db.run(units.result)

  def exists(unitId: String): Future[Boolean] =
    db.run(units.filter(_.unitId === unitId).exists.result)

  def save(unit: Unit): Future[Unit] = ???
}
