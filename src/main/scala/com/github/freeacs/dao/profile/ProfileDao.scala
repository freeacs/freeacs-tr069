package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProfileDao(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Dao with ProfileTable {

  import config.profile.api._

  def list(): Future[Seq[Profile]] =
    db.run(profiles.result)

  def save(profile: Profile): Future[Profile] =
    db.run(profiles returning profiles.map(_.profileId)
      into ((profile, id) => profile.copy(profileId = Some(id)))
      += profile)
}