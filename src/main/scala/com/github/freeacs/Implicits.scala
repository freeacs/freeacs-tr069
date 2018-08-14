package com.github.freeacs
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import com.github.jarlah.authenticscala.AuthenticationContext
import com.github.jarlah.authenticscala.Authenticator.{Basic, Digest, Mode}

import scala.collection.immutable
import scala.language.implicitConversions

object Implicits {

  implicit def fromMapToHeaders(
      headers: Map[String, String]
  ): immutable.Seq[HttpHeader] =
    headers.map(header => RawHeader(header._1, header._2)).to[immutable.Seq]

  implicit def fromStringToMode(mode: String): Mode =
    mode match {
      case "digest" => Digest
      case "basic"  => Basic
    }

  implicit def fromHttpRequestToAuthenticationContext(
      request: HttpRequest
  ): AuthenticationContext =
    AuthenticationContext(
      request.method.value,
      request.uri.toString(),
      request.headers.map(h => (h.name() -> h.value())).toMap,
      ""
    )
}
