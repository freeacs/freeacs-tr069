package com.github.freeacs.repository

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DbConfiguration {
  lazy val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
}