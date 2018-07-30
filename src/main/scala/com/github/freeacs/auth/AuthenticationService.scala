package com.github.freeacs.auth

import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationService(implicit val ec: ExecutionContext) {

  def myUserPassAuthenticator(credentials: Credentials): Future[Option[String]] =
    credentials match {
      case p @ Credentials.Provided(id) if p.verify(id) => Future.successful(Some(id))
      case _ => Future.successful(None)
    }

}