package com.github.freeacs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DummyAuthService extends AuthService(null) {
  override def authenticator(
      user: String,
      verify: String => Future[Boolean]
  ): Future[Either[String, Unit]] =
    Future.successful(Right(()))

  override def getSecret(
      user: String
  ): Future[Option[String]] = Future.successful(Some(user))
}
