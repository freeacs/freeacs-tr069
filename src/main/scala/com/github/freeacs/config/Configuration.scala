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
      val dbConfig        = DatabaseConfig.forConfig[JdbcProfile]("db", config)
      private val conf    = config.getConfig("server")
      val responseTimeout = getDuration(conf, "response.timeout")
      val actorTimeout    = getDuration(conf, "actor.timeout")
      val maxFailures     = conf.getInt("circuit-breaker.maxFailures")
      val callTimeout     = getDuration(conf, "circuit-breaker.callTimeout")
      val resetTimeout    = getDuration(conf, "circuit-breaker.resetTimeout")
      val hostname        = conf.getString("http.host")
      val port            = conf.getInt("http.port")
      val authMethod      = conf.getString("auth.method")
      val name            = conf.getString("name")
      val mode            = conf.getString("http.mode")
    }

  private def getDuration(config: Config, key: String): FiniteDuration = {
    config.getDuration("actor.timeout").toMillis millis
  }
}
