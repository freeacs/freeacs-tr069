package com.github.freeacs.repositories

import com.github.freeacs.domain.profile.ACSProfile
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class ProfileDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  val unitTypeDao = new UnitTypeDao(config)

  implicit val getProfileResult = GetResult(
    r => ACSProfile(r.<<, unitTypeDao.getUnitTypeResult(r), r.<<)
  )

  val tableName = "profile"

  def columns(prefix: Option[String] = None) =
    s"$tableName.profile_name, ${unitTypeDao.columns(Some(unitTypeDao.tableName))}, $tableName.profile_id"

  private val columnsStr = columns(None)

  def getAllQuery: DBIO[Seq[ACSProfile]] =
    sql"""select #$columnsStr from #$tableName""".as[ACSProfile]

  def getAll: Future[Seq[ACSProfile]] = db.run(getAllQuery)

  def getByIdQuery(profileId: Long) =
    sql"""select #$columnsStr from #$tableName as #$tableName, #${unitTypeDao.tableName} as #${unitTypeDao.tableName}
          where #$tableName.profile_id = $profileId and #$tableName.unit_type_id = #${unitTypeDao.tableName}.unit_type_id
      """.as[ACSProfile].headOption

  def getById(profileId: Long): Future[Option[ACSProfile]] =
    db.run(getByIdQuery(profileId))

  def getByNameQuery(profileName: String) =
    sql"""select #$columnsStr from #$tableName
          where profile_name= '$profileName'
       """.as[ACSProfile].headOption

  def getByName(profileName: String): Future[Option[ACSProfile]] =
    db.run(getByNameQuery(profileName))
}
