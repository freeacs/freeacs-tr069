package com.github.freeacs.services

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def authenticator(
      user: String,
      verify: (String) => Future[Boolean]
  ): Future[Either[String, Unit]]
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
        verify: (String) => Future[Boolean]
    ): Future[Either[String, Unit]] =
      services.getUnitSecret(user).flatMap {
        case Some(secret) =>
          verify(secret).map {
            case true => Right()
            case _    => Left("Wrong username or password")
          }.recover[Either[String, Unit]] {
            case _ =>
              Left("Error occurred while trying to verify secret")
          }
        case _ =>
          Future.successful(Left("No secret found"))
      }
  }
}
