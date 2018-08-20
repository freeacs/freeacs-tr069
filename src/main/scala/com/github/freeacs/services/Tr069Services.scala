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
  def getUnit(unitId: String): Future[Option[Unit]]

  def getUnitSecret(unitId: String): Future[Option[String]]

  def getUnitParameters(unitId: String): Future[Seq[UnitParameter]]

  def createUnitType(name: String): Future[UnitType]

  def createProfile(name: String, unitTypeId: Long): Future[Profile]

  def getUnitTypeByName(name: String): Future[Option[UnitType]]

  def createUnitTypeParameters(
      params: Seq[(String, String)],
      unitTypeId: Long
  ): Future[Seq[UnitTypeParameter]]

  def createUnit(userId: String): Future[Unit]

  def createOrUpdateUnitParameters(
      unitParams: Seq[(String, String, Long)]
  ): Future[Int]

  def getUnitTypeParameters(
      unitTYpeId: Long
  ): Future[Seq[UnitTypeParameter]]
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
      ): Future[Seq[UnitTypeParameter]] = {
        unitTypeParameterRepository.readByUnitType(unitTYpeId).map { params =>
          params.map(
            utp =>
              UnitTypeParameter(
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

      def getUnit(unitId: String): Future[Option[Unit]] =
        unitRepository.getByUnitId(unitId)

      def getUnitParameters(unitId: String): Future[Seq[UnitParameter]] =
        unitParameterRepository
          .getUnitParameters(unitId)
          .map(list => {
            list.map(
              tuple =>
                UnitParameter(
                  tuple._1.unitId,
                  UnitTypeParameter(
                    tuple._2.name,
                    tuple._2.flags,
                    tuple._2.unitTypeId,
                    tuple._2.unitTypeParameterId
                  ),
                  tuple._1.value
              )
            )
          })

      def createUnitType(name: String): Future[UnitType] =
        unitTypeRepository.save(
          UnitType(
            unitTypeName = name,
            description = Some("Auto generated"),
            protocol = "TR069"
          )
        )

      def createProfile(name: String, unitTypeId: Long): Future[Profile] =
        ???

      def getUnitTypeByName(name: String): Future[Option[UnitType]] =
        unitTypeRepository.getByName(name)

      def createUnitTypeParameters(
          params: Seq[(String, String)],
          unitTypeId: Long
      ): Future[Seq[UnitTypeParameter]] =
        unitTypeParameterRepository
          .save(
            params.map(p => unitType.UnitTypeParameter(p._1, p._2, unitTypeId))
          )
          .map(
            _.map(
              p =>
                UnitTypeParameter(
                  p.name,
                  p.flags,
                  p.unitTypeId,
                  p.unitTypeParameterId
              )
            )
          )

      def createUnit(userId: String): Future[Unit] = ???
    }

  private def toDomainUnit(
      daoUnit: Unit,
      daoUnitType: UnitType,
      daoProfile: Any,
      unitTypeParams: Seq[UnitTypeParameter],
      unitParams: Seq[UnitParameter]
  ): Some[Unit] = {
    ???
  }
}
