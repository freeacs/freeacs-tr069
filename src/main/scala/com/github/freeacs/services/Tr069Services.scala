package com.github.freeacs.services

import com.github.freeacs.dao.profile.{ProfileDao, ProfileParameterDao}
import com.github.freeacs.dao.unit.{UnitDao, UnitParameterDao}
import com.github.freeacs.dao.unitType.{UnitTypeDao, UnitTypeParameterDao}
import com.github.freeacs.domain
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait Tr069Services {
  def getUnit(unitId: String): Future[Option[domain.Unit]]
  def getUnitSecret(unitId: String): Future[Option[String]]
  def getUnitParameters(unitId: String): Future[Seq[domain.UnitParameter]]
}

object Tr069Services {
  def from(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext): Tr069Services =
    new Tr069ServicesImpl(dbConfig)
}

private class Tr069ServicesImpl(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends Tr069Services {
  val unitTypeRepository = new UnitTypeDao(dbConfig)
  val unitTypeParameterRepository = new UnitTypeParameterDao(dbConfig)

  val profileRepository = new ProfileDao(dbConfig)
  val profileParameterRepository = new ProfileParameterDao(dbConfig)

  val unitRepository = new UnitDao(dbConfig)
  val unitParameterRepository = new UnitParameterDao(dbConfig)

  def getUnitSecret(unitId: String): Future[Option[String]] =
    unitParameterRepository.getUnitSecret(unitId)

  def getUnit(unitId: String): Future[Option[domain.Unit]] =
    unitRepository.get(unitId).map {
      case Some((unit, unitType, profile)) =>
        Some(
          domain.Unit(
            unit.unitId,
            domain.UnitType(unitType.unitTypeId, unitType.unitTypeName),
            domain.Profile(profile.profileId, profile.profileName, profile.unitTypeId)
          )
        )
      case _ =>
        None
    }

  def getUnitParameters(unitId: String): Future[Seq[domain.UnitParameter]] =
    unitParameterRepository.getUnitParameters(unitId).map(list => {
      list.map(tuple =>
        domain.UnitParameter(tuple._1.unitId,
          domain.UnitTypeParameter(
            tuple._2.unitTypeParameterId,
            tuple._2.unitTypeId,
            tuple._2.name,
            tuple._2.flags
          ), tuple._1.value)
      )
    })
}
