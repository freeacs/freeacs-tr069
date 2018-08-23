package com.github.freeacs.repositories

import com.github.freeacs.domain.unitTypeParameter.ACSUnitTypeParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  implicit val getUnitTypeParamResult = GetResult(
    r => ACSUnitTypeParameter(r.<<, r.<<, r.<<, r.<<?)
  )

  val tableName = "unit_type_param"

  def columns(prefix: Option[String] = None) =
    super.getColumns(
      Seq("name", "flags", "unit_type_id", "unit_type_param_id"),
      prefix
    )

  def getByUnitTypeId(unitTypeId: Long): DBIO[Seq[ACSUnitTypeParameter]] =
    sql"""
         select #${columns()} from #$tableName
         where unit_type_id = $unitTypeId
       """.as[ACSUnitTypeParameter]

  def getAllQuery: DBIO[Seq[ACSUnitTypeParameter]] =
    sql"""select #${columns()} from #$tableName""".as[ACSUnitTypeParameter]

  def getAll: Future[Seq[ACSUnitTypeParameter]] = db.run(getAllQuery)
}
