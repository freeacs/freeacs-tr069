package com.github.freeacs.services

import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.Future

trait AuthenticationService {
  def authenticator(credentials: Credentials): Future[Option[String]]
}
