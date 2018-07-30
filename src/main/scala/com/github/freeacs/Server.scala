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

  private val maxFailures = 3
  private val callTimeout = 1.seconds
  private val resetTimeout = 10.seconds
  private val cb = new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)

  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  private val tr069Services = new Tr069Services(dbConfig)
  private val tr069Routes = new Tr069Routes(cb, tr069Services)

  private val config = ConfigFactory.load()
  private val hostname = config.getString("http.host")
  private val port = config.getInt("http.port")
  private val server = Http().bindAndHandle(tr069Routes.routes, hostname, port)

  StdIn.readLine()

  server.flatMap(_.unbind)
  system.terminate()

}
