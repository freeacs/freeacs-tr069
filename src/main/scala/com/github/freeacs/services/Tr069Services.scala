package com.github.freeacs.services

import com.github.freeacs.dao.profile.{
  ProfileDao,
  ProfileParameterDao,
  Profile => ProfileDTO
}
import com.github.freeacs.dao.unit.{UnitDao, UnitParameterDao}
import com.github.freeacs.dao.{unit, unitType}
import com.github.freeacs.dao.unitType.{
  UnitTypeDao,
  UnitTypeParameterDao,
  UnitType => UnitTypeDTO
}
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
        unitRepository.get(unitId).flatMap {
          case Some((unit, unitType, profile)) =>
            for {
              unitTypeParams <- getUnitTypeParameters(unit.unitTypeId)
              unitParams     <- getUnitParameters(unitId)
            } yield
              toDomainUnit(
                unit,
                unitType,
                profile,
                unitTypeParams,
                unitParams
              )
          case _ =>
            Future.successful(None)
        }

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
        unitTypeRepository
          .save(
            UnitTypeDTO(
              unitTypeName = name,
              description = Some("Auto generated"),
              protocol = "TR069"
            )
          )
          .map(
            dto =>
              UnitType(
                dto.unitTypeName,
                dto.protocol,
                dto.unitTypeId,
                dto.matcherId,
                dto.vendorName,
                dto.description
            )
          )

      def createProfile(name: String, unitTypeId: Long): Future[Profile] =
        profileRepository
          .save(ProfileDTO(profileName = name, unitTypeId = unitTypeId))
          .map(dto => Profile(dto.profileName, dto.unitTypeId, dto.profileId))

      def getUnitTypeByName(name: String): Future[Option[UnitType]] =
        unitTypeRepository.getByName(name).map {
          case Some(dto) =>
            Some(
              UnitType(
                dto._1.unitTypeName,
                dto._1.protocol,
                dto._1.unitTypeId,
                dto._1.matcherId,
                dto._1.vendorName,
                dto._1.description,
                params = dto._2.map(p => {
                  UnitTypeParameter(
                    p.name,
                    p.flags,
                    p.unitTypeId,
                    p.unitTypeParameterId
                  )
                })
              )
            )
          case _ =>
            None
        }

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
      daoUnit: unit.Unit,
      daoUnitType: UnitTypeDTO,
      daoProfile: ProfileDTO,
      unitTypeParams: Seq[UnitTypeParameter],
      unitParams: Seq[UnitParameter]
  ): Some[Unit] = {
    Some(
      Unit(
        daoUnit.unitId,
        UnitType(
          daoUnitType.unitTypeName,
          daoUnitType.protocol,
          daoUnitType.unitTypeId,
          daoUnitType.matcherId,
          daoUnitType.vendorName,
          daoUnitType.description,
          unitTypeParams
        ),
        Profile(
          daoProfile.profileName,
          daoProfile.unitTypeId,
          daoProfile.profileId
        ),
        unitParams
      )
    )
  }
}
