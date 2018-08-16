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
  val name: String
  val mode: String

}

object Configuration {
  def from(config: Config): Configuration =
    new Configuration {
      val dbConfig     = DatabaseConfig.forConfig[JdbcProfile]("db", config)
      private val conf = config.getConfig("server")
      val responseTimeout: FiniteDuration =
        conf.getDuration("response.timeout").toMillis millis
      val actorTimeout: FiniteDuration =
        conf.getDuration("actor.timeout").toMillis millis
      val maxFailures: Int = conf.getInt("circuit-breaker.maxFailures")
      val callTimeout: FiniteDuration =
        conf.getDuration("circuit-breaker.callTimeout").toMillis millis
      val resetTimeout: FiniteDuration =
        conf.getDuration("circuit-breaker.resetTimeout").toMillis millis
      val hostname: String   = conf.getString("http.host")
      val port: Int          = conf.getInt("http.port")
      val authMethod: String = conf.getString("auth.method")
      val name: String       = conf.getString("name")
      val mode               = conf.getString("http.mode")
    }
}
