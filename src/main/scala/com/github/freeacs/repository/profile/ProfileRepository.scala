package com.github.freeacs.repository.profile

import com.github.freeacs.repository.Database
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProfileRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Database with ProfileTable {

  import config.profile.api._

  def list(): Future[Seq[Profile]] =
    db.run(profiles.result)

  def save(profile: Profile): Future[Profile] = ???
}