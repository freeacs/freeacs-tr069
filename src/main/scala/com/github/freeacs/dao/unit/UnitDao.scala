package com.github.freeacs.dao.unit

import com.github.freeacs.dao.Dao
import com.github.freeacs.dao.profile.{Profile, ProfileTable}
import com.github.freeacs.dao.unitType.UnitType
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao
    with UnitTable
    with ProfileTable {

  import config.profile.api._

  def list(): Future[Seq[Unit]] =
    db.run(units.result)

  def exists(unitId: String): Future[Boolean] =
    db.run(units.filter(_.unitId === unitId).exists.result)

  def save(unit: Unit): Future[Unit] = ???

  def get(unitId: String): Future[Option[(Unit, UnitType, Profile)]] =
    db.run(
      units
        .filter(_.unitId === unitId)
        .join(unitTypes)
        .on(_.unitTypeId === _.unitTypeId)
        .join(profiles)
        .on(_._1.profileId === _.profileId)
        .map(tuple => (tuple._1._1, tuple._1._2, tuple._2))
        .result
        .headOption
    )
}
