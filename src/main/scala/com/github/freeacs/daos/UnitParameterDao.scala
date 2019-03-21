package com.github.freeacs.daos

import com.github.freeacs.config.SystemParameters
import com.github.freeacs.domain.unitParameter.ACSUnitParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  val utpDao = new UnitTypeParameterDao(config)

  implicit val getUnitParameterResult = GetResult(
    r =>
      ACSUnitParameter(
        r.<<,
        utpDao.getUnitTypeParamResult(r),
        r.<<?
    )
  )

  val tableName    = "unit_param"
  val utpTableName = utpDao.tableName
  val utpColumns   = utpDao.columns(Some(utpTableName))
  val columns      = s"$tableName.unit_id, $utpColumns, $tableName.value"

  def getByUnitIdQuery(unitId: String): DBIO[Seq[ACSUnitParameter]] =
    sql"""select #$columns
          from   #$tableName as #$tableName, #$utpTableName as #$utpTableName
          where  #$tableName.unit_type_param_id = #$utpTableName.unit_type_param_id and
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
  def createOrUpdate(tuple: ACSUnitParameter): DBIO[Int] = {
    val unitId          = tuple.unitId
    val unitTypeParamId = tuple.unitTypeParameter.id.get
    val unitParamValue  = tuple.value.getOrElse("")
    sqlu"""insert into #$tableName(unit_id, unit_type_param_id, value)
           values('#$unitId', #$unitTypeParamId, '#$unitParamValue')
           ON DUPLICATE KEY UPDATE value='#$unitParamValue';"""
  }

  def createOrUpdateUnitParams(
      params: Seq[ACSUnitParameter]
  ): Future[Int] =
    db.run(DBIO.sequence(params.map(createOrUpdate)).transactionally).map(_.sum)
}
