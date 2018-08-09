package com.github.freeacs

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.CircuitBreaker
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.FiniteDuration

class AppSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val authenticationService = new DummyAuthenticationService()
  val duration = FiniteDuration(1, TimeUnit.SECONDS)
  val breaker = new CircuitBreaker(system.scheduler, 1,  duration,  duration)
  val responseTimeout = FiniteDuration(1, TimeUnit.SECONDS)
  val actorTimeout = FiniteDuration(1, TimeUnit.SECONDS)
  val routes = new Routes(breaker ,null, authenticationService, null).routes

  "The session actor" should {
    "return Invalid request on a POST to /tr069 without any body" in {
      // tests:
      Post("/tr069") ~> routes ~> check {
        responseAs[String] shouldEqual "Invalid request"
      }
    }
  }
}
