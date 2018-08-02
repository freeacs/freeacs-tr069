package com.github.freeacs.services


import com.github.freeacs.domain.{UnitParameter, UnitTypeParameter}
import com.github.freeacs.repository.profile.{ProfileParameterRepository, ProfileRepository}
import com.github.freeacs.repository.unit.{UnitParameterRepository, UnitRepository}
import com.github.freeacs.repository.unitType.{UnitTypeParameterRepository, UnitTypeRepository}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class Tr069Services(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  val unitTypeRepository = new UnitTypeRepository(dbConfig)
  val unitTypeParameterRepository = new UnitTypeParameterRepository(dbConfig)

  val profileRepository = new ProfileRepository(dbConfig)
  val profileParameterRepository = new ProfileParameterRepository(dbConfig)

  val unitRepository = new UnitRepository(dbConfig)
  val unitParameterRepository = new UnitParameterRepository(dbConfig)

  def getUnitParameters(unitId: String): Future[Seq[UnitParameter]] =
    unitParameterRepository.getUnitParameters(unitId).map(list => {
      list.map(tuple => {
        val up = tuple._1
        val utp = tuple._2
        UnitParameter(up.unitId,
          UnitTypeParameter(
            utp.unitTypeParameterId,
            utp.unitTypeId,
            utp.name,
            utp.flags
          ), up.value)
      })
    })

  val authService = new AuthenticationService(this)
}
