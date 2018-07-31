package com.github.freeacs.repository.unit

import com.github.freeacs.config.SystemParameters
import com.github.freeacs.repository.Db
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UnitParameterRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends Db with UnitParameterTable {

  import config.profile.api._

  def list(): Future[Seq[UnitParameter]] =
    db.run(unitParameters.result)

  def getUnitSecret(unitId: String): Future[Option[String]] =
    db.run(unitParameters.join(unitTypeParameters).on(_.unitTypeParamId === _.unitTypeParamId)
      .filter(_._2.name === SystemParameters.SECRET)
      .map(_._1.value)
      .result.headOption)

  def getUnitParameters(unitId: String): Future[Seq[UnitParameter]] =
    db.run(unitParameters.filter(_.unitId === unitId).result)

  def updateUnitParameters(params: Seq[UnitParameter]): Future[Int] =
    db.run(DBIO.sequence(params.map(p => unitParameters.insertOrUpdate(p))).transactionally)
      .map(_.sum)
}
