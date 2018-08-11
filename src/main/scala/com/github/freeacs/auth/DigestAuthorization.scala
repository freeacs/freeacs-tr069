package com.github.freeacs.auth

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.util.Timeout
import com.github.freeacs.actors.Conversation.{GetNonceCreated, SetNonce}

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object DigestAuthorization {

  def unauthorizedDigest(
      remoteIp: String,
      realm: String,
      qop: String,
      digestSecret: String,
      nonceActor: ActorRef
  ) = {
    HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = immutable.Seq(
        DigestAuthorization.getDigestHeader(
          remoteIp,
          realm,
          qop,
          digestSecret,
          nonceActor
        )
      )
    )
  }

  def verifyDigest(
      username: String,
      params: Map[String, String],
      realm: String,
      nonceActor: ActorRef,
      nonceTTL: Long
  )(
      secret: String
  )(
      implicit ec: ExecutionContext,
      timeout: Timeout = FiniteDuration(1, TimeUnit.SECONDS)
  ): Future[Boolean] = {
    val nonce    = params("nonce")
    val nc       = params("nc")
    val cnonce   = params("cnonce")
    val qop      = params("qop")
    val uri      = params("uri")
    val response = params("response")
    val method   = "POST"
    (nonceActor ? GetNonceCreated(nonce)).map(res => {
      res.asInstanceOf[Option[Long]].exists {
        time =>
          if (System.currentTimeMillis() - time > nonceTTL) {
            false
          } else {
            val sharedSecret = {
              if (secret != null && secret.length > 16 && !(passwordMd5(
                    username,
                    secret,
                    method,
                    uri,
                    nonce,
                    nc,
                    cnonce,
                    qop,
                    realm
                  ) == response))
                secret.substring(0, 16)
              else
                secret
            }
            passwordMd5(
              username,
              sharedSecret,
              method,
              uri,
              nonce,
              nc,
              cnonce,
              qop,
              realm
            ).equals(response)
          }
      }
    })
  }

  def getDigestHeader(
      remoteIp: String,
      realm: String,
      qop: String,
      digestSecret: String,
      nonceActor: ActorRef
  ): RawHeader = {
    val nonce = DigestUtils.md5Hex(
      s"$remoteIp:${System.currentTimeMillis}:$digestSecret"
    )
    nonceActor ! SetNonce(
      System.currentTimeMillis(),
      nonce
    )
    val opaque = DigestUtils.md5Hex(nonce)
    val authHeader =
      s"""Digest realm="$realm", qop="$qop", nonce="$nonce", opaque="$opaque""""
    RawHeader("WWW-Authenticate", authHeader)
  }

  def passwordMd5(
      username: String,
      password: String,
      method: String,
      uri: String,
      nonce: String,
      nc: String,
      cnonce: String,
      qop: String,
      realm: String
  ): String = {
    val a1    = s"$username:$realm:$password"
    val md5a1 = DigestUtils.md5Hex(a1)
    val a2    = s"$method:$uri"
    val md5a2 = DigestUtils.md5Hex(a2)
    DigestUtils.md5Hex(s"$md5a1:$nonce:$nc:$cnonce:$qop:$md5a2")
  }

  /**
   * Convert the authentication username to unitid (should be 1:1, but there might be some
   * vendor specific problems to solve...
   *
   * @throws UnsupportedEncodingException thrown if url decoding fails
   */
  def username2unitId(username: String): String = {
    try URLDecoder.decode(username, "UTF-8")
    catch {
      case _: UnsupportedEncodingException =>
        username
    }
  }
}
