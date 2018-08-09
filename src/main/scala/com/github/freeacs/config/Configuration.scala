package com.github.freeacs.config

import com.typesafe.config.Config
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

trait Configuration {

  val dbConfig: DatabaseConfig[JdbcProfile]
  val responseTimeout: FiniteDuration
  val actorTimeout: FiniteDuration
  val maxFailures: Int
  val callTimeout: FiniteDuration
  val resetTimeout: FiniteDuration
  val hostname: String
  val port: Int
  val authMethod: String
  val digestRealm: String
  val digestQop: String
  val digestSecret: String
  val basicRealm: String

}

object Configuration {
  def from(config: Config): Configuration =
    new ConfigurationImpl(config)

  private[this] class ConfigurationImpl(config: Config) extends Configuration {

    val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db", config)
    private val serverConfig = config.getConfig("server")
    val responseTimeout: FiniteDuration = serverConfig.getDuration("response.timeout").toMillis millis
    val actorTimeout: FiniteDuration = serverConfig.getDuration("actor.timeout").toMillis millis
    val maxFailures: Int = serverConfig.getInt("circuit-breaker.maxFailures")
    val callTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.callTimeout").toMillis millis
    val resetTimeout: FiniteDuration = serverConfig.getDuration("circuit-breaker.resetTimeout").toMillis millis
    val hostname: String = serverConfig.getString("http.host")
    val port: Int = serverConfig.getInt("http.port")
    val basicRealm: String = serverConfig.getString("auth.basic.realm")
    val digestRealm: String = serverConfig.getString("auth.digest.realm")
    val digestQop: String = serverConfig.getString("auth.digest.qop")
    val digestSecret: String = serverConfig.getString("auth.digest.secret")
    val authMethod: String = serverConfig.getString("auth.method")
  }
}
