package com.github.freeacs.repositories
import com.github.freeacs.domain.ACSUnit
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  val unitTypeDao      = new UnitTypeDao(config)
  val profileDao       = new ProfileDao(config)
  val unitParameterDao = new UnitParameterDao(config)

  import config.profile.api._

  implicit val getUnitResult = GetResult(
    r =>
      ACSUnit(
        r.<<,
        unitTypeDao.getUnitTypeResult(r),
        profileDao.getProfileResult(r)
    )
  )

  val tableName         = "unit"
  val profileTableName  = profileDao.tableName
  val unitTypeTableName = unitTypeDao.tableName
  val profileColumns    = profileDao.columns(Some(profileTableName))
  val unitTypeColumns   = unitTypeDao.columns(Some(unitTypeTableName))
  val columns           = s"$tableName.unit_id, $unitTypeColumns, $profileColumns"

  def getByUnitIdQuery(unitId: String) =
    sql"""select #$columns
          from   #$tableName as #$tableName,
                 #$unitTypeTableName as #$unitTypeTableName,
                 #$profileTableName as #$profileTableName
          where  #$tableName.unit_id = '#$unitId' and
                 #$tableName.profile_id = #$profileTableName.profile_id and
                 #$tableName.unit_type_id = #$unitTypeTableName.unit_type_id;
       """.as[ACSUnit].headOption

  def getByUnitId(unitId: String): Future[Option[ACSUnit]] =
    db.run(for {
      unit   <- getByUnitIdQuery(unitId)
      params <- unitParameterDao.getByUnitIdQuery(unitId)
    } yield unit.map(_.copy(params = params)))

  def getAllQuery: DBIO[Seq[ACSUnit]] =
    sql"""select #$columns from #$tableName""".as[ACSUnit]

  def getAll: Future[Seq[ACSUnit]] = db.run(getAllQuery)
}
