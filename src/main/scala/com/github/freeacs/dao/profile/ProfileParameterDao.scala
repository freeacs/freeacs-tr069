package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProfileParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao
    with ProfileParameterTable {

  import config.profile.api._

  def list(): Future[Seq[ProfileParameter]] =
    db.run(profileParameters.result)
}
