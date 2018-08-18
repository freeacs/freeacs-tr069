package com.github.freeacs
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, Unauthorized}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import com.github.jarlah.authenticscala.{
  AuthenticationContext,
  AuthenticationResult
}
import com.github.jarlah.authenticscala.Authenticator.{
  authenticate,
  challenge,
  PasswordRetriever
}

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait Auth { self: Directives =>

  def authenticateConversation(
      retriever: PasswordRetriever,
      authMethod: String
  )(route: (String) => Route)(implicit ec: ExecutionContext): Route =
    extractAuthenticationContext { (context) =>
      onComplete(authenticate(context, retriever, authMethod)) {
        case Success(AuthenticationResult(success, maybeUser, maybeError)) =>
          if (success && maybeUser.isDefined) {
            route(maybeUser.get)
          } else {
            complete(
              HttpResponse(
                Unauthorized,
                challenge(context, authMethod)
                  .map(header => RawHeader(header._1, header._2))
                  .to[immutable.Seq]
              ).withEntity(maybeError.getOrElse(""))
            )
          }
        case Failure(_) =>
          complete(HttpResponse(InternalServerError))
      }
    }

  def extractAuthenticationContext(
      route: AuthenticationContext => Route
  ): Route =
    (extractRequest & extractClientIP) { (request, remoteIp) =>
      route(
        AuthenticationContext(
          request.method.value,
          request.uri.toString(),
          request.headers.map(h => (h.name() -> h.value())).toMap,
          remoteIp.toIP.map(_.ip.getHostAddress).getOrElse("Unknown")
        )
      )
    }
}
