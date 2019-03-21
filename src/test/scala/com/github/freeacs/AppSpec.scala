package com.github.freeacs

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.CircuitBreaker
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.UnitService
import com.github.freeacs.session.SessionService
import com.typesafe.config.Config
import org.scalatest.{Matchers, WordSpec}
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class AppSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val duration = FiniteDuration(1, TimeUnit.SECONDS)
  val breaker  = new CircuitBreaker(system.scheduler, 1, duration, duration)
  val configuration = new Configuration {
    val dbConfig = new DatabaseConfig[JdbcProfile]() {
      override def db: JdbcBackend#DatabaseDef = null
      override val profile: JdbcProfile        = null
      override val driver: JdbcProfile         = null
      override def config: Config              = null
      override def profileName: String         = null
      override def profileIsObject: Boolean    = false
    }
    val responseTimeout = FiniteDuration(1, TimeUnit.SECONDS)
    val actorTimeout    = FiniteDuration(1, TimeUnit.SECONDS)
    val maxFailures     = 1
    val callTimeout     = FiniteDuration(1, TimeUnit.SECONDS)
    val resetTimeout    = FiniteDuration(1, TimeUnit.SECONDS)
    val hostname        = "test"
    val port            = -1
    val authMethod      = "basic"
    val name            = "test"
    val mode            = "chunked"
  }
  val routes = new Routes(
    breaker,
    new UnitService(configuration.dbConfig) {
      override def getUnitSecret(
          unitId: String
      ): Future[Option[
        String
      ]] = Future.successful(Some(unitId))
    },
    configuration,
    new SessionService(null, configuration, null)
  ).routes

  "The server" should {
    "return 401 Unauthorized on a POST to /tr069 without any authorization" in {
      // tests:
      Post("/tr069") ~> routes ~> check {
        response.status shouldEqual StatusCodes.Unauthorized
      }
    }
  }
}
