package com.github.freeacs.repositories

import com.github.freeacs.domain.ACSProfileParameter
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class ProfileParameterDao(val config: DatabaseConfig[JdbcProfile])(
    implicit ec: ExecutionContext
) extends Dao {

  import config.profile.api._

  implicit val getProfileParameterResult = GetResult(
    r => ACSProfileParameter.fromResultSet(r.<<, r.<<, r.<<?)
  )

  val tableName = "profile_param"

  def columns(prefix: Option[String] = None) =
    super.getColumns(Seq("profile_id", "unit_type_param_id", "value"), prefix)

  def getAllQuery: DBIO[Seq[ACSProfileParameter]] =
    sql"""select #${columns()} from #$tableName""".as[ACSProfileParameter]

  def getAll: Future[Seq[ACSProfileParameter]] = db.run(getAllQuery)
}
