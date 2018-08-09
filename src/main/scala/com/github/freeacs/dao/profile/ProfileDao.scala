package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ProfileDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext)
    extends Dao
    with ProfileTable
    with ProfileParameterTable {

  import config.profile.api._

  def list(): Future[Seq[Profile]] =
    db.run(profiles.result)

  def getByNameAndUnitType(
      name: String,
      unitTypeId: Long): Future[Option[(Profile, Seq[ProfileParameter])]] =
    for {
      profile <- db.run(
        profiles
          .filter(p => p.profileName === name && p.unitTypeId === unitTypeId)
          .result
          .headOption)
      params <- {
        if (profile.isDefined)
          db.run(
            profileParameters
              .filter(pp => pp.profileId === profile.get.profileId)
              .result)
        else
          Future.successful(Seq.empty)
      }
    } yield
      (
        profile.map(p => {
          (p, params)
        })
      )

  def save(profile: Profile): Future[Profile] =
    db.run(
      profiles returning profiles.map(_.profileId)
        into ((profile, id) => profile.copy(profileId = Some(id)))
        += profile)
}
