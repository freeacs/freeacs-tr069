package com.github.freeacs.daos

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait Dao {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db

  def getColumns(columns: Seq[String], prefix: Option[String] = None): String =
    columns
      .map(
        col =>
          s"${prefix.map(_ + ".").getOrElse("")}$col as ${prefix.map(_ + "_").getOrElse("")}$col"
      )
      .mkString(", ")
}
