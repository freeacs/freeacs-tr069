package com.github.freeacs.services

import com.github.freeacs.repository.profile.{ProfileParameterRepository, ProfileRepository}
import com.github.freeacs.repository.unit.{UnitParameterRepository, UnitRepository}
import com.github.freeacs.repository.unitType.{UnitTypeParameterRepository, UnitTypeRepository}
import com.typesafe.config.Config
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class Tr069Services(dbConfig: DatabaseConfig[JdbcProfile], sysConfig: Config)(implicit ec: ExecutionContext) {
  val unitTypeRepository = new UnitTypeRepository(dbConfig)
  val unitTypeParameterRepository = new UnitTypeParameterRepository(dbConfig)

  val profileRepository = new ProfileRepository(dbConfig)
  val profileParameterRepository = new ProfileParameterRepository(dbConfig)

  val unitRepository = new UnitRepository(dbConfig)
  val unitParameterRepository = new UnitParameterRepository(dbConfig)

  val authService = new AuthenticationService(this)
}
