package com.github.freeacs.services

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def authenticator(user: String, pass: String): Future[Either[Unit, Unit]]
}

object AuthenticationService {
  def from(services: Tr069Services)(implicit ex: ExecutionContext): AuthenticationService =
    new AuthenticationServiceImpl(services)

  private[this] class AuthenticationServiceImpl(services: Tr069Services)(implicit ex: ExecutionContext) extends AuthenticationService {

    def authenticator(user: String, pass: String): Future[Either[Unit, Unit]] =
      services.getUnitSecret(user)
        .map {
          case Some(secret) if pass.equals(secret) =>
            Right()
          case _ =>
            Left()
        }
  }
}