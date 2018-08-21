package com.github.freeacs.repositories

import com.github.freeacs.domain._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class DaoService(dbConfig: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) {
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
