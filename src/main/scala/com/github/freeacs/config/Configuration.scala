package com.github.freeacs.config

import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.language.postfixOps

class Configuration(config: Config) {

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db", config)

  private val serverConfig = config.getConfig("server")

  val sessionLookupTimeout: FiniteDuration = serverConfig.getDuration("session.lookup.timeout").toMillis millis

  val maxFailures: Int = serverConfig.getInt("circuit-breaker.maxFailures")
  val callTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.callTimeout").toMillis millis
  val resetTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.resetTimeout").toMillis millis

  val hostname: String = serverConfig.getString("http.host")
  val port: Int = serverConfig.getInt("http.port")
}