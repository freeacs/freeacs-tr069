package com.github.freeacs

import com.github.freeacs.repositories.DaoService

import scala.concurrent.{ExecutionContext, Future}

class AuthService(services: DaoService)(implicit ex: ExecutionContext) {
  type Verifier = String => Future[Boolean]

  def authenticator(
      user: String,
      verify: Verifier
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

  def getSecret(user: String): Future[Option[String]] =
    services.getUnitSecret(user)
}
