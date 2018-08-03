package com.github.freeacs

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.CircuitBreaker
import com.github.freeacs.routes.Tr069Routes
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.FiniteDuration

class AppSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val authenticationService = new DummyAuthenticationService()
  val breaker = new CircuitBreaker(system.scheduler, 1,  FiniteDuration(1, TimeUnit.SECONDS),  FiniteDuration(1, TimeUnit.SECONDS))
  val sessionLookupTimeout = FiniteDuration(1, TimeUnit.SECONDS)
  val routes = new Tr069Routes(breaker ,null, authenticationService, sessionLookupTimeout).routes

  "The session actor" should {
    "return Invalid request on a POST to /tr069 without any body" in {
      // tests:
      Post("/tr069") ~> routes ~> check {
        responseAs[String] shouldEqual "Invalid request"
      }
    }
  }
}
