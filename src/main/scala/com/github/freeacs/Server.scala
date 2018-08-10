package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.Cache
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.postfixOps

trait Server {

  implicit val system: ActorSystem          = ActorSystem("freeacs-http")
  implicit val mat: ActorMaterializer       = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val cache: Cache[String, Long]   = LfuCache[String, Long]
  implicit val config: Configuration        = Configuration.from(ConfigFactory.load())

  import config._

  val breaker =
    new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
  val services    = Tr069Services.from(dbConfig)
  val authService = AuthenticationService.from(services)
  val routes      = new Routes(breaker, services, authService, config, cache)

  val server = Http().bindAndHandle(routes.routes, hostname, port)
  StdIn.readLine()
  server.flatMap(_.unbind)
  system.terminate()

}
