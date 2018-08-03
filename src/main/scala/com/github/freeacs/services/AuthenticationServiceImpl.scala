package com.github.freeacs.services

import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationServiceImpl(services: Tr069Services)(implicit ex: ExecutionContext) extends AuthenticationService {

  def authenticator(credentials: Credentials): Future[Option[String]] = {
    credentials match {
      case p@Credentials.Provided(id) =>
        services.getUnitSecret(id)
          .map {
            case Some(secret) if p.verify(secret) =>
              Some(id)
            case _ =>
              None
          }
      case _ =>
        Future.successful(None)
    }
  }

}