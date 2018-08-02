package com.github.freeacs.services

import com.github.freeacs.domain
import com.github.freeacs.repository.profile.{ProfileParameterRepository, ProfileRepository}
import com.github.freeacs.repository.unit.{UnitParameterRepository, UnitRepository}
import com.github.freeacs.repository.unitType.{UnitTypeParameterRepository, UnitTypeRepository}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class Tr069Services(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  val unitTypeRepository = new UnitTypeRepository(dbConfig)
  val unitTypeParameterRepository = new UnitTypeParameterRepository(dbConfig)

  val profileRepository = new ProfileRepository(dbConfig)
  val profileParameterRepository = new ProfileParameterRepository(dbConfig)

  val unitRepository = new UnitRepository(dbConfig)
  val unitParameterRepository = new UnitParameterRepository(dbConfig)

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

  val authService = new AuthenticationService(this)
}
