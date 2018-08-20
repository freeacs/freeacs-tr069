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

  implicit val getUnitParameterResult = GetResult(
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
    db.run(
      getByUnitIdQuery(unitId)
        .filter(
          _.exists(_.unitTypeParameter.name == SystemParameters.SECRET)
        )
        .map(_.headOption match {
          case Some(p) => p.value
          case _       => None
        })
    )
  def createOrUpdate(param: ACSUnitParameter): DBIO[Int] =
    sqlu"""insert into #$tableName(unit_id, unit_type_param_id, value)
           values('#${param.unitId}', #${param.unitTypeParameter.unitTypeParamId.get}, '#${param.value
      .getOrElse("")}')
           ON DUPLICATE KEY UPDATE value='#${param.value.getOrElse("")}';"""

  def createOrUpdateUnitParams(params: Seq[ACSUnitParameter]): Future[Int] =
    db.run(DBIO.sequence(params.map(createOrUpdate)).transactionally).map(_.sum)
}
