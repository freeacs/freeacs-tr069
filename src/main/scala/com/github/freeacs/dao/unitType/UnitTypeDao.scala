package com.github.freeacs.dao.unitType

import com.github.freeacs.dao.Dao
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class UnitTypeDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao
    with UnitTypeTable
    with UnitTypeParameterTable {

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

  def getByName2(
      name: String
  ): Future[Option[(UnitType, Seq[UnitTypeParameter])]] =
    for {
      unitType <- db.run(
                   unitTypes.filter(_.unitTypeName === name).result.headOption
                 )
      params <- {
        if (unitType.isDefined)
          db.run(
            unitTypeParameters
              .filter(_.unitTypeId === unitType.get.unitTypeId)
              .result
          )
        else
          Future.successful(Seq.empty)
      }
    } yield
      (
        unitType.map(ut => {
          (ut, params)
        })
      )

  def save(unitType: UnitType): Future[UnitType] =
    db.run(
      unitTypes returning unitTypes.map(_.unitTypeId)
        into ((unitType, id) => unitType.copy(unitTypeId = Some(id)))
        += unitType
    )
}
