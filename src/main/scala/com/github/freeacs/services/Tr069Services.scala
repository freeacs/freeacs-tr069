package com.github.freeacs.services

import com.github.freeacs.domain

import scala.concurrent.Future

trait Tr069Services {
  def getUnit(unitId: String): Future[Option[domain.Unit]]
  def getUnitSecret(unitId: String): Future[Option[String]]
  def getUnitParameters(unitId: String): Future[Seq[domain.UnitParameter]]
}
