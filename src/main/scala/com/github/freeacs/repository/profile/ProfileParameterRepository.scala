package com.github.freeacs.repository.profile

import com.github.freeacs.repository.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProfileParameterRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Db with ProfileParameterTable {

  import config.profile.api._

  def list(): Future[Seq[ProfileParameter]] =
    db.run(profileParameters.result)
}