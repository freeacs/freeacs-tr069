package com.github.freeacs

import com.github.freeacs.services.AuthenticationService

import scala.concurrent.Future

class DummyAuthenticationService extends AuthenticationService {
  override def authenticator(user: String, pass: String): Future[Either[Unit, Unit]] = Future.successful(Right())
}