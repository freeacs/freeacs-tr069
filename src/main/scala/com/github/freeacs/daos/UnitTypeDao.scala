package com.github.freeacs.daos

import com.github.freeacs.domain.unitType.ACSUnitType
import com.github.freeacs.domain.unitType.ACSUnitType.Protocol
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  implicit val getUnitTypeProtocol = GetResult(
    r =>
      r.<<[String] match {
        case Protocol.TR069.name => Protocol.TR069
        case _                   => Protocol.OTHER
    }
  )

  implicit val getUnitTypeResult = GetResult(
    r => ACSUnitType(r.<<, r.<<, r.<<?, r.<<?, r.<<?, r.<<?)
  )

  val tableName = "unit_type"

  def columns(prefix: Option[String] = None): String =
    super.getColumns(
      Seq(
        "unit_type_name",
        "protocol",
        "unit_type_id",
        "description",
        "matcher_id",
        "vendor_name"
      ),
      prefix
    )

  private val columnsStr = columns()

  def getByIdQuery(id: Long): DBIO[Option[ACSUnitType]] =
    sql"""select #$columnsStr from #$tableName
          where unit_type_id = id
       """.as[ACSUnitType].headOption

  def getById(id: Long): Future[Option[ACSUnitType]] =
    db.run(getByIdQuery(id))

  def getByNameQuery(name: String): DBIO[Option[ACSUnitType]] =
    sql"""select #$columnsStr from #$tableName
          where unit_type_name = '$name'
       """.as[ACSUnitType].headOption

  def getByName(name: String): Future[Option[ACSUnitType]] =
    db.run(getByNameQuery(name))
}
