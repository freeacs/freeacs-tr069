package com.github.freeacs.auth

import akka.http.scaladsl.server.directives.Credentials
import com.github.freeacs.repository.unit.UnitParameterRepository

import scala.concurrent.Future

class AuthenticationService(unitParameterRepository: UnitParameterRepository) {

  def myUserPassAuthenticator(credentials: Credentials): Future[Option[String]] =
    credentials match {
      case p @ Credentials.Provided(id) if p.verify(id) =>
        Future.successful(Some(id))
      case _ =>
        Future.successful(None)
    }

}