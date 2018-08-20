package com.github.freeacs.services

import com.github.freeacs.domain._
import com.github.freeacs.repositories._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait Tr069Services {
  def getUnit(unitId: String): Future[Option[ACSUnit]]

  def getUnitSecret(unitId: String): Future[Option[String]]

  def createOrUpdateUnitParameters(
      unitParams: Seq[(String, String, Long)]
  ): Future[Int]
}

object Tr069Services {
  def from(
      dbConfig: DatabaseConfig[JdbcProfile]
  )(implicit ec: ExecutionContext): Tr069Services =
    new Tr069Services {

      val unitRepository          = new UnitDao(dbConfig)
      val unitParameterRepository = new UnitParameterDao(dbConfig)

      def createOrUpdateUnitParameters(
          unitParams: Seq[(String, String, Long)]
      ): Future[Int] =
        unitParameterRepository.createOrUpdateUnitParams(
          unitParams.map(
            up =>
              ACSUnitParameter(
                up._1,
                ACSUnitTypeParameter("", "", -1, Option(up._3)),
                Option(up._2)
            )
          )
        )

      def getUnitSecret(unitId: String): Future[Option[String]] =
        unitParameterRepository.getUnitSecret(unitId)

      def getUnit(unitId: String): Future[Option[ACSUnit]] =
        unitRepository.getByUnitId(unitId)
    }
}
