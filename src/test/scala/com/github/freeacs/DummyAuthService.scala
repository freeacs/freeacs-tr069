package com.github.freeacs

import com.github.freeacs.services.AuthService

import scala.concurrent.Future

class DummyAuthService extends AuthService {
  def authenticator(
      user: String,
      verify: String => Future[Boolean]
  ): Future[Either[String, Unit]] =
    Future.successful(Right(()))

  def getSecret(
      user: String
  ): Future[Option[String]] = Future.successful(Some(user))
}
