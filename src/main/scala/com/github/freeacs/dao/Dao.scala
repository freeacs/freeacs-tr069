package com.github.freeacs.dao

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait Dao {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db
}
