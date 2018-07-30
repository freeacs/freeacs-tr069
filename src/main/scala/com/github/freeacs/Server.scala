package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.routes.Tr069Routes
import com.github.freeacs.services.Tr069Services
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{FiniteDuration, _}
import scala.io.StdIn

object Server extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val maxFailures: Int = 3
  val callTimeout: FiniteDuration = 1.seconds
  val resetTimeout: FiniteDuration = 10.seconds
  val cb = new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")

  val tr069Services = new Tr069Services(dbConfig)

  val tr069Routes = new Tr069Routes(cb, tr069Services)

  val config = ConfigFactory.load()
  val hostname = config.getString("http.host")
  val port = config.getInt("http.port")
  val server = Http().bindAndHandle(tr069Routes.routes, hostname, port)

  StdIn.readLine()

  server.flatMap(_.unbind)
  system.terminate()

}
