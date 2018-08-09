package com.github.freeacs.auth

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader

import scala.collection.immutable

object BasicAuthorization {
  def decodeBasicAuth(authHeader: String) = {
    val baStr                 = authHeader.replaceFirst("Basic ", "")
    val decoded               = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }

  def unauthorizedBasic(realm: String) =
    HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = immutable.Seq(
        RawHeader("WWW-Authenticate", s"""Basic realm="$realm"""")
      )
    )
}
