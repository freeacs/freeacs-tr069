package com.github.freeacs.repository.unitType

import com.github.freeacs.repository.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeParameterRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Db with UnitTypeParameterTable {

  import config.profile.api._

  def list(): Future[Seq[UnitTypeParameter]] =
    db.run(unitTypeParameters.result)
}
