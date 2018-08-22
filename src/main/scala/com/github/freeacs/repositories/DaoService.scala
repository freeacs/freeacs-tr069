package com.github.freeacs.repositories

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import com.github.freeacs.domain._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import akka.util.Timeout
import com.github.freeacs.session.SessionCache.{
  Cached,
  GetFromCache,
  PutInCache
}

import scala.concurrent.duration.FiniteDuration

class DaoService(dbConfig: DatabaseConfig[JdbcProfile], cache: ActorRef)(
    implicit ec: ExecutionContext
) {
  implicit val timeout: Timeout = FiniteDuration(1, TimeUnit.SECONDS)

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
    (cache ? GetFromCache("unit-" + unitId)).flatMap {
      case Cached(_: String, Some(unit)) =>
        Future.successful(Some(unit.asInstanceOf[ACSUnit]))
      case _ =>
        unitRepository
          .getByUnitId(unitId)
          .map(unit => {
            cache ! PutInCache("unit-" + unitId, unit)
            unit
          })
    }
}
