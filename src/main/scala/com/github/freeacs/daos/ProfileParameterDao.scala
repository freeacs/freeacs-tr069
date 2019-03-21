package com.github.freeacs.daos

import com.github.freeacs.domain.profileParameter.ACSProfileParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.ExecutionContext

class ProfileParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  val unitTypeParameterDao = new UnitTypeParameterDao(config)

  implicit val getProfileParameterResult = GetResult(
    r =>
      ACSProfileParameter(
        r.<<,
        unitTypeParameterDao.getUnitTypeParamResult(r),
        r.<<?
    )
  )

  val tableName = "profile_param"

  def columns(prefix: Option[String] = None): String =
    super.getColumns(Seq("profile_id", "unit_type_param_id", "value"), prefix)
}
