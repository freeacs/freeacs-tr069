package com.github.freeacs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.github.freeacs.session.{SessionCache, SessionService}
import com.github.freeacs.config.Configuration
import com.github.freeacs.repositories.DaoService
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.postfixOps

trait Server {

  implicit val system: ActorSystem          = ActorSystem("freeacs-http")
  implicit val mat: ActorMaterializer       = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val config: Configuration        = Configuration.from(ConfigFactory.load())

  import config._

  val breaker = new CircuitBreaker(
    system.scheduler,
    maxFailures,
    callTimeout,
    resetTimeout
  )

  val services     = new DaoService(dbConfig)
  val authService  = new AuthService(services)
  val cacheActor   = system.actorOf(SessionCache.props)
  val conversation = new SessionService(services, config, cacheActor)

  val routes = new Routes(
    breaker,
    services,
    authService,
    config,
    conversation
  )

  val server = Http().bindAndHandle(routes.routes, hostname, port)

  StdIn.readLine()
  server.flatMap(_.unbind)

  system.terminate()

}
