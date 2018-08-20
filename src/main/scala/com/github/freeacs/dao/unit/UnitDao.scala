package com.github.freeacs.dao.unit

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.profile.ProfileDao
import com.github.freeacs.dao.unitType.UnitTypeDao
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}
import com.github.freeacs.domain.Unit

import scala.concurrent.{ExecutionContext, Future}

class UnitDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  val unitTypeDao = new UnitTypeDao(config)
  val profileDao  = new ProfileDao(config)

  import config.profile.api._

  implicit val getUnitResult = GetResult(
    r =>
      Unit(
        r.<<,
        unitTypeDao.getUnitTypeResult(r),
        profileDao.getProfileResult(r)
    )
  )

  val tableName         = "unit"
  val profileTableName  = profileDao.tableName
  val unitTypeTableName = unitTypeDao.tableName
  val profileColumns    = profileDao.columns(profileTableName)
  val unitTypeColumns   = unitTypeDao.columns(unitTypeTableName)
  val columns           = s"$tableName.unit_id, $unitTypeColumns, $profileColumns"

  def getByUnitIdQuery(unitId: String) =
    sql"""select #$columns
          from   #$tableName as #$tableName,
                 #$unitTypeTableName as #$unitTypeTableName,
                 #$profileTableName as #$profileTableName
          where  #$tableName.unit_id = '#$unitId' and
                 #$tableName.profile_id = #$profileTableName.profile_id and
                 #$tableName.unit_type_id = #$unitTypeTableName.unit_type_id;
       """.as[Unit].headOption

  def getByUnitId(unitId: String): Future[Option[Unit]] =
    db.run(for {
      unit <- getByUnitIdQuery(unitId)
    } yield unit)

  def getAllQuery: DBIO[Seq[Unit]] =
    sql"""select #$columns from #$tableName""".as[Unit]

  def getAll: Future[Seq[Unit]] = db.run(getAllQuery)
}
