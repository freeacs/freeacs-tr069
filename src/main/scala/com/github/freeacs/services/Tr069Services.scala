package com.github.freeacs.services

import com.github.freeacs.domain._
import com.github.freeacs.repositories._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait Tr069Services {
  def getUnit(unitId: String): Future[Option[ACSUnit]]

  def getUnitSecret(unitId: String): Future[Option[String]]

  def getUnitParameters(unitId: String): Future[Seq[ACSUnitParameter]]

  def createUnitType(name: String): Future[ACSUnitType]

  def createProfile(name: String, unitTypeId: Long): Future[ACSProfile]

  def getUnitTypeByName(name: String): Future[Option[ACSUnitType]]

  def createUnitTypeParameters(
      params: Seq[(String, String)],
      unitTypeId: Long
  ): Future[Seq[ACSUnitTypeParameter]]

  def createUnit(userId: String): Future[ACSUnit]

  def createOrUpdateUnitParameters(
      unitParams: Seq[(String, String, Long)]
  ): Future[Int]

  def getUnitTypeParameters(
      unitTYpeId: Long
  ): Future[Seq[ACSUnitTypeParameter]]
}

object Tr069Services {
  def from(
      dbConfig: DatabaseConfig[JdbcProfile]
  )(implicit ec: ExecutionContext): Tr069Services =
    new Tr069Services {
      val unitTypeRepository          = new UnitTypeDao(dbConfig)
      val unitTypeParameterRepository = new UnitTypeParameterDao(dbConfig)
      val profileRepository           = new ProfileDao(dbConfig)
      val profileParameterRepository  = new ProfileParameterDao(dbConfig)
      val unitRepository              = new UnitDao(dbConfig)
      val unitParameterRepository     = new UnitParameterDao(dbConfig)

      def createOrUpdateUnitParameters(
          unitParams: Seq[(String, String, Long)]
      ): Future[Int] = {
        Future.successful(0)
      }

      def getUnitTypeParameters(
          unitTYpeId: Long
      ): Future[Seq[ACSUnitTypeParameter]] = {
        Future.successful(Seq.empty)
      }

      def getUnitSecret(unitId: String): Future[Option[String]] =
        unitParameterRepository.getUnitSecret(unitId)

      def getUnit(unitId: String): Future[Option[ACSUnit]] =
        unitRepository.getByUnitId(unitId)

      def getUnitParameters(unitId: String): Future[Seq[ACSUnitParameter]] =
        Future.successful(Seq.empty)

      def createUnitType(name: String): Future[ACSUnitType] =
        unitTypeRepository.save(
          ACSUnitType(
            unitTypeName = name,
            description = Some("Auto generated"),
            protocol = "TR069"
          )
        )

      def createProfile(name: String, unitTypeId: Long): Future[ACSProfile] =
        ???

      def getUnitTypeByName(name: String): Future[Option[ACSUnitType]] =
        unitTypeRepository.getByName(name)

      def createUnitTypeParameters(
          params: Seq[(String, String)],
          unitTypeId: Long
      ): Future[Seq[ACSUnitTypeParameter]] =
        Future.successful(Seq.empty)

      def createUnit(userId: String): Future[ACSUnit] = ???
    }

  private def toDomainUnit(
      daoUnit: ACSUnit,
      daoUnitType: ACSUnitType,
      daoProfile: Any,
      unitTypeParams: Seq[ACSUnitTypeParameter],
      unitParams: Seq[ACSUnitParameter]
  ): Some[ACSUnit] = {
    ???
  }
}
