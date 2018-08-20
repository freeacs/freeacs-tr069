package com.github.freeacs.services

import com.github.freeacs.dao.profile.{ProfileDao, ProfileParameterDao}
import com.github.freeacs.dao.unit.{UnitDao, UnitParameterDao}
import com.github.freeacs.dao.{unit, unitType}
import com.github.freeacs.dao.unitType.{UnitTypeDao, UnitTypeParameterDao}
import com.github.freeacs.domain._
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
        unitParameterRepository.updateUnitParameters(
          unitParams.map(
            up =>
              unit.UnitParameter(
                up._1,
                up._3,
                Option(up._2)
            )
          )
        )
      }

      def getUnitTypeParameters(
          unitTYpeId: Long
      ): Future[Seq[ACSUnitTypeParameter]] = {
        unitTypeParameterRepository.readByUnitType(unitTYpeId).map { params =>
          params.map(
            utp =>
              ACSUnitTypeParameter(
                utp.name,
                utp.flags,
                utp.unitTypeId,
                utp.unitTypeParameterId
            )
          )
        }
      }

      def getUnitSecret(unitId: String): Future[Option[String]] =
        unitParameterRepository.getUnitSecret(unitId)

      def getUnit(unitId: String): Future[Option[ACSUnit]] =
        unitRepository.getByUnitId(unitId)

      def getUnitParameters(unitId: String): Future[Seq[ACSUnitParameter]] =
        unitParameterRepository
          .getUnitParameters(unitId)
          .map(list => {
            list.map(
              tuple =>
                ACSUnitParameter(
                  tuple._1.unitId,
                  ACSUnitTypeParameter(
                    tuple._2.name,
                    tuple._2.flags,
                    tuple._2.unitTypeId,
                    tuple._2.unitTypeParameterId
                  ),
                  tuple._1.value
              )
            )
          })

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
        unitTypeParameterRepository
          .save(
            params.map(p => unitType.UnitTypeParameter(p._1, p._2, unitTypeId))
          )
          .map(
            _.map(
              p =>
                ACSUnitTypeParameter(
                  p.name,
                  p.flags,
                  p.unitTypeId,
                  p.unitTypeParameterId
              )
            )
          )

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
