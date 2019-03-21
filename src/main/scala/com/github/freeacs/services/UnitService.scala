package com.github.freeacs.services

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.github.freeacs.domain.unit.ACSUnit
import com.github.freeacs.domain.unitParameter.ACSUnitParameter
import com.github.freeacs.daos.{UnitDao, UnitParameterDao}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class UnitService(dbConfig: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) {
  implicit val timeout: Timeout = FiniteDuration(1, TimeUnit.SECONDS)

  val unitRepository          = new UnitDao(dbConfig)
  val unitParameterRepository = new UnitParameterDao(dbConfig)

  def createOrUpdateUnitParameters(
      unitParams: Seq[ACSUnitParameter]
  ): Future[Int] =
    unitParameterRepository.createOrUpdateUnitParams(unitParams)

  def getUnitSecret(unitId: String): Future[Option[String]] =
    unitParameterRepository.getUnitSecret(unitId)

  def getUnit(unitId: String): Future[Option[ACSUnit]] =
    unitRepository.getByUnitId(unitId)
}
