package com.github.freeacs

import com.github.freeacs.services.AuthenticationService

import scala.concurrent.Future

class DummyAuthenticationService extends AuthenticationService {
  def authenticator(user: String, verify: String => Boolean): Future[Either[String, (String, String)]] = Future.successful(Right(("easycwmp", "easycwmp")))
}