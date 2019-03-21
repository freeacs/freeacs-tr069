package com.github.freeacs.daos
import com.github.freeacs.domain.unit.ACSUnit
import com.github.freeacs.domain.unitTypeParameter.ACSUnitTypeParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  val unitTypeDao          = new UnitTypeDao(config)
  val profileDao           = new ProfileDao(config)
  val unitParameterDao     = new UnitParameterDao(config)
  val unitTypeParameterDao = new UnitTypeParameterDao(config)

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
    db.run(
      for {
        unit           <- getByUnitIdQuery(unitId)
        unitParams     <- unitParameterDao.getByUnitIdQuery(unitId)
        unitTypeParams <- getUnitTypeParams(unit)
      } yield
        unit.map(
          u =>
            u.copy(
              params = unitParams,
              unitType = u.unitType.copy(params = unitTypeParams)
          )
        )
    )

  private def getUnitTypeParams(
      unit: Option[ACSUnit]
  ): DBIO[Seq[ACSUnitTypeParameter]] =
    unit match {
      case Some(acsUnit) =>
        unitTypeParameterDao.getByUnitTypeId(acsUnit.unitType.id.get)
      case _ =>
        DBIO.successful(Seq.empty)
    }
}
