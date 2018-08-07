package com.github.freeacs.services

import com.github.freeacs.dao.profile.{ProfileDao, ProfileParameterDao, Profile => ProfileDTO}
import com.github.freeacs.dao.unit.{UnitDao, UnitParameterDao}
import com.github.freeacs.dao.unitType.{UnitTypeDao, UnitTypeParameterDao, UnitType => UnitTypeDTO}
import com.github.freeacs.domain._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait Tr069Services {
  def getUnit(unitId: String): Future[Option[Unit]]

  def getUnitSecret(unitId: String): Future[Option[String]]

  def getUnitParameters(unitId: String): Future[Seq[UnitParameter]]

  def createUnitType(name: String): Future[UnitType]

  def createProfile(name: String, unitTypeId: Long): Future[Profile]
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

  def getUnit(unitId: String): Future[Option[Unit]] =
    unitRepository.get(unitId).map {
      case Some((unit, unitType, profile)) =>
        Some(
          Unit(
            unit.unitId,
            UnitType(unitType.unitTypeId, unitType.unitTypeName, unitType.matcherId, unitType.vendorName, unitType.description, unitType.protocol),
            Profile(profile.profileId, profile.profileName, profile.unitTypeId)
          )
        )
      case _ =>
        None
    }

  def getUnitParameters(unitId: String): Future[Seq[UnitParameter]] =
    unitParameterRepository.getUnitParameters(unitId).map(list => {
      list.map(tuple =>
        UnitParameter(tuple._1.unitId,
          UnitTypeParameter(
            tuple._2.unitTypeParameterId,
            tuple._2.unitTypeId,
            tuple._2.name,
            tuple._2.flags
          ), tuple._1.value)
      )
    })

  def createUnitType(name: String): Future[UnitType] =
    unitTypeRepository.save(UnitTypeDTO(unitTypeName = name, description = Some("Auto generated"), protocol = "TR069"))
      .map(dto => {
        UnitType(
          dto.unitTypeId,
          dto.unitTypeName,
          dto.matcherId,
          dto.vendorName,
          dto.description,
          dto.protocol
        )
      })

  def createProfile(name: String, unitTypeId: Long): Future[Profile] =
    profileRepository.save(ProfileDTO(profileName = name, unitTypeId = unitTypeId))
    .map(dto => {
      Profile(dto.profileId, dto.profileName, dto.unitTypeId)
    })
}
