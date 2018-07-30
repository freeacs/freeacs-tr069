package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.auth.AuthenticationService
import com.github.freeacs.routes.Tr069Routes
import com.github.freeacs.services.Tr069Services
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.io.StdIn

object Server extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val maxFailures = 3
  val callTimeout = 1.seconds
  val resetTimeout = 10.seconds
  val cb = new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val tr069Services = new Tr069Services(dbConfig)
  val authService = new AuthenticationService(tr069Services.unitParameterRepository)
  val tr069Routes = new Tr069Routes(cb, tr069Services, authService)

  val config = ConfigFactory.load()
  val hostname = config.getString("http.host")
  val port = config.getInt("http.port")
  val server = Http().bindAndHandle(tr069Routes.routes, hostname, port)

  StdIn.readLine()

  server.flatMap(_.unbind)
  system.terminate()

}
