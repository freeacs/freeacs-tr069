package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.config.{Configuration, ConfigurationImpl}
import com.github.freeacs.routes.Tr069Routes
import com.github.freeacs.services.{AuthenticationServiceImpl, Tr069ServicesImpl}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.postfixOps

trait Server {

  implicit val system: ActorSystem = ActorSystem("freeacs-http")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val configuration: Configuration = new ConfigurationImpl(ConfigFactory.load())

  import configuration._

  val breaker = new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
  val services = new Tr069ServicesImpl(dbConfig)
  val authService = new AuthenticationServiceImpl(services)
  val routes = new Tr069Routes(breaker, services, authService, sessionLookupTimeout)

  val server = Http().bindAndHandle(routes.routes, hostname, port)
  StdIn.readLine()
  server.flatMap(_.unbind)
  system.terminate()

}