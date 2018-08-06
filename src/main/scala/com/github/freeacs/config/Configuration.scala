package com.github.freeacs.config

import com.typesafe.config.Config
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

trait Configuration {

  val dbConfig: DatabaseConfig[JdbcProfile]
  val timeout: FiniteDuration
  val maxFailures: Int
  val callTimeout: FiniteDuration
  val resetTimeout: FiniteDuration
  val hostname: String
  val port: Int

}

object Configuration {
  def from(config: Config): Configuration = new ConfigurationImpl(config)
}

private class ConfigurationImpl(config: Config) extends Configuration {

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db", config)
  private val serverConfig = config.getConfig("server")
  val timeout: FiniteDuration = serverConfig.getDuration("timeout").toMillis millis
  val maxFailures: Int = serverConfig.getInt("circuit-breaker.maxFailures")
  val callTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.callTimeout").toMillis millis
  val resetTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.resetTimeout").toMillis millis
  val hostname: String = serverConfig.getString("http.host")
  val port: Int = serverConfig.getInt("http.port")

}
