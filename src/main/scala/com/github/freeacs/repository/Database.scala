package com.github.freeacs.repository

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait Database {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db
}
