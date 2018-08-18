package com.github.freeacs.services

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {
  def authenticator(
      user: String,
      verify: (String) => Future[Boolean]
  ): Future[Either[String, Unit]]

  def getSecret(user: String): Future[Option[String]]
}

object AuthService {
  type Verifier = String => Future[Boolean]

  def from(
      services: Tr069Services
  )(implicit ex: ExecutionContext): AuthService =
    new AuthService {
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
}
