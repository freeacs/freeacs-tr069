package com.github.freeacs

import akka.http.scaladsl.server.directives.Credentials
import com.github.freeacs.services.AuthenticationService

import scala.concurrent.Future

class DummyAuthenticationService extends AuthenticationService {
  override def authenticator(credentials: Credentials): Future[Option[String]] = Future.successful(Some("easycwmp"))
}