package com.github.freeacs.repositories

import com.github.freeacs.config.SystemParameters
import com.github.freeacs.domain.ACSUnit.UnitId
import com.github.freeacs.domain.ACSUnitParameter
import com.github.freeacs.domain.ACSUnitParameter.{
  ACSUnitParameterTupleType,
  UnitParameterValue
}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  val utpDao = new UnitTypeParameterDao(config)

  implicit val getUnitId = GetResult(
    r => UnitId(r.<<)
  )

  implicit val unitParamValueResult = GetResult(
    r => r.<<?[String].map(UnitParameterValue.apply)
  )

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
  def createOrUpdate(tuple: ACSUnitParameterTupleType): DBIO[Int] = {
    val unitId          = tuple._1
    val unitTypeParamId = tuple._2
    val unitParamValue  = tuple._3.getOrElse("")
    sqlu"""insert into #$tableName(unit_id, unit_type_param_id, value)
           values('#$unitId', #$unitTypeParamId, '#$unitParamValue')
           ON DUPLICATE KEY UPDATE value='#$unitParamValue';"""
  }

  def createOrUpdateUnitParams(
      params: Seq[ACSUnitParameterTupleType]
  ): Future[Int] =
    db.run(DBIO.sequence(params.map(createOrUpdate)).transactionally).map(_.sum)
}
