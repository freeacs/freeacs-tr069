package com.github.freeacs.services

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def authenticator(
      user: String,
      verify: (String) => Boolean
  ): Future[Either[String, (String, String)]]
}

object AuthenticationService {
  def from(
      services: Tr069Services
  )(implicit ex: ExecutionContext): AuthenticationService =
    new AuthenticationServiceImpl(services)

  private[this] class AuthenticationServiceImpl(services: Tr069Services)(
      implicit ex: ExecutionContext
  ) extends AuthenticationService {

    def authenticator(
        user: String,
        verify: (String) => Boolean
    ): Future[Either[String, (String, String)]] =
      services.getUnitSecret(user).map {
        case Some(secret) if verify(secret) =>
          Right((user, secret))
        case _ =>
          Left("Wrong username and/or password")
      }
  }
}
