package com.github.freeacs.dao.unit

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import com.github.freeacs.domain.Unit
import scala.concurrent.{ExecutionContext, Future}

class UnitDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  def list(): Future[Seq[Unit]] = ???

  def exists(unitId: String): Future[Boolean] = ???

  def save(unit: Unit): Future[Unit] = ???

  def get(unitId: String): Future[Option[Unit]] = ???
}
