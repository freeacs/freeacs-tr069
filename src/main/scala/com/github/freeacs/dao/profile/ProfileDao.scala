package com.github.freeacs.dao.profile

import com.github.freeacs.dao.Dao
import com.github.freeacs.domain.Profile
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class ProfileDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao
    with ProfileParameterTable {

  import config.profile.api._

  implicit val getProfileResult = GetResult(r => Profile(r.<<, r.<<, r.<<))

  val tableName = "profile"

  def columns(prefix: String) =
    Seq("profile_name", "unit_type_id", "profile_id")
      .map(
        col => s"$prefix.$col as ${prefix}_$col"
      )
      .mkString(", ")

  def getAllQuery: DBIO[Seq[Profile]] =
    sql"""select #${columns("")} from #$tableName""".as[Profile]

  def getAll: Future[Seq[Profile]] = db.run(getAllQuery)

  def getByIdQuery(profileId: Long) =
    sql"""select #${columns("")} from #$tableName
          where profile_id = $profileId""".as[Profile].headOption

  def getById(profileId: Long): Future[Option[Profile]] =
    db.run(getByIdQuery(profileId))

  def getByNameQuery(profileName: String) =
    sql"""select #${columns("")} from #$tableName
          where profile_name= '$profileName'
       """.as[Profile].headOption

  def getByName(profileName: String): Future[Option[Profile]] =
    db.run(getByNameQuery(profileName))
}
