package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import com.github.freeacs.domain.UnitType
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  implicit val getUnitTypeResult = GetResult(
    r => UnitType(r.<<, r.<<, r.<<?, r.<<?, r.<<?, r.<<?)
  )

  val tableName = "unit_type"

  val columns =
    "unit_type_name, protocol, unit_type_id, description, matcher_id, vendor_name"

  def getAllQuery: DBIO[Seq[UnitType]] =
    sql"""select #$columns from #$tableName""".as[UnitType]

  def getAll: Future[Seq[UnitType]] = db.run(getAllQuery)

  def getByIdQuery(id: Long): DBIO[Option[UnitType]] =
    sql"""select #$columns from #$tableName
          where unit_type_id = id
       """.as[UnitType].headOption

  def getById(id: Long): Future[Option[UnitType]] =
    db.run(getByIdQuery(id))

  def getByNameQuery(name: String): DBIO[Option[UnitType]] =
    sql"""select #$columns from #$tableName
          where unit_type_name = '$name'
       """.as[UnitType].headOption

  def getByName(name: String): Future[Option[UnitType]] =
    db.run(getByNameQuery(name))

  def save(unitType: UnitType): Future[UnitType] =
    ???
}
