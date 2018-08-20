package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import com.github.freeacs.domain.ACSUnitType
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  implicit val getUnitTypeResult = GetResult(
    r => ACSUnitType(r.<<, r.<<, r.<<?, r.<<?, r.<<?, r.<<?)
  )

  val tableName = "unit_type"

  def columns(prefix: String) =
    Seq(
      "unit_type_name",
      "protocol",
      "unit_type_id",
      "description",
      "matcher_id",
      "vendor_name"
    ).map(col => s"$prefix.$col as ${prefix}_$col").mkString(", ")

  def getAllQuery: DBIO[Seq[ACSUnitType]] =
    sql"""select #${columns("")} from #$tableName""".as[ACSUnitType]

  def getAll: Future[Seq[ACSUnitType]] = db.run(getAllQuery)

  def getByIdQuery(id: Long): DBIO[Option[ACSUnitType]] =
    sql"""select #${columns("")} from #$tableName
          where unit_type_id = id
       """.as[ACSUnitType].headOption

  def getById(id: Long): Future[Option[ACSUnitType]] =
    db.run(getByIdQuery(id))

  def getByNameQuery(name: String): DBIO[Option[ACSUnitType]] =
    sql"""select #${columns("")} from #$tableName
          where unit_type_name = '$name'
       """.as[ACSUnitType].headOption

  def getByName(name: String): Future[Option[ACSUnitType]] =
    db.run(getByNameQuery(name))

  def save(unitType: ACSUnitType): Future[ACSUnitType] =
    ???
}
