package com.github.freeacs.services

import com.github.freeacs.repository.profile.{ProfileParameterRepository, ProfileRepository}
import com.github.freeacs.repository.unit.{UnitParameterRepository, UnitRepository}
import com.github.freeacs.repository.unitType.{UnitTypeParameterRepository, UnitTypeRepository}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class Tr069Services(config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  val unitTypeRepository = new UnitTypeRepository(config)
  val unitTypeParameterRepository = new UnitTypeParameterRepository(config)

  val profileRepository = new ProfileRepository(config)
  val profileParameterRepository = new ProfileParameterRepository(config)

  val unitRepository = new UnitRepository(config)
  val unitParameterRepository = new UnitParameterRepository(config)


}
