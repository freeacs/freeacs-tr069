package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.config.Configuration
import com.github.freeacs.routes.Tr069Routes
import com.github.freeacs.services.Tr069Services
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.postfixOps

object Server extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val Configuration = new Configuration(ConfigFactory.load())
  import Configuration._

  val cb = new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)

  val services = new Tr069Services(dbConfig)
  val routes = new Tr069Routes(cb, services, sessionLookupTimeout)

  val server = Http().bindAndHandle(routes.routes, hostname, port)

  StdIn.readLine()

  server.flatMap(_.unbind)
  system.terminate()

}
