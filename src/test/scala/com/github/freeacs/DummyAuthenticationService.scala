package com.github.freeacs

import com.github.freeacs.services.AuthenticationService

import scala.concurrent.Future

class DummyAuthenticationService extends AuthenticationService {
  def authenticator(
      user: String,
      verify: String => Future[Boolean]
  ): Future[Either[String, Unit]] =
    Future.successful(Right(()))

  def getSecret(
      user: String
  ): Future[String] = Future.successful(user)
}
