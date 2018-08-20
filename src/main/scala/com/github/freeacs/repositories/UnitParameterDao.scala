package com.github.freeacs.repositories

import com.github.freeacs.config.SystemParameters
import com.github.freeacs.domain.ACSUnitParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  val unitTypeParameterDao = new UnitTypeParameterDao(config)

  implicit val getProfileParameterResult = GetResult(
    r =>
      ACSUnitParameter(
        r.<<,
        unitTypeParameterDao.getUnitTypeParamResult(r),
        r.<<?
    )
  )

  val tableName              = "unit_param"
  val unitTypeParamTableName = unitTypeParameterDao.tableName
  val unitTypeParamColumns =
    unitTypeParameterDao.columns(Some(unitTypeParamTableName))
  val columns = s"$tableName.unit_id, $unitTypeParamColumns, $tableName.value"

  def getByUnitIdQuery(unitId: String): DBIO[Seq[ACSUnitParameter]] =
    sql"""select #$columns
          from   #$tableName as #$tableName, #$unitTypeParamTableName as #$unitTypeParamTableName
          where  #$tableName.unit_type_param_id = #$unitTypeParamTableName.unit_type_param_id and 
                 #$tableName.unit_id = '#$unitId';
      """.as[ACSUnitParameter]

  def getByUnitId(unitId: String): Future[Seq[ACSUnitParameter]] =
    db.run(getByUnitIdQuery(unitId))

  def getUnitSecret(unitId: String): Future[Option[String]] =
    Future.successful(Some("easycwmp"))
}
