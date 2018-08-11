package com.github.freeacs.actors
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.Cache

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

sealed trait Command
case class GetNonceTTL(nonce: String)
case class SetNonceTTL(nonce: String, ttl: Long)

object NonceActor {
  def props = Props(new NonceActor)

  def getNonceActor(actorTimeout: FiniteDuration)(
      implicit system: ActorSystem,
      ec: ExecutionContext
  ): Future[ActorRef] =
    system.actorSelection(s"user/nonce").resolveOne(actorTimeout).recover {
      case _: Exception =>
        system.actorOf(props, "nonce")
    }
}

class NonceActor extends Actor with ActorLogging {
  implicit val system = context.system
  implicit val ec     = context.dispatcher

  val cache: Cache[String, Long] = LfuCache[String, Long]

  def receive: Receive = {
    case SetNonceTTL(nonce, ttl) =>
      cache.getOrLoad(nonce, _ => Future.successful(ttl))
    case GetNonceTTL(nonce) =>
      val maybeF   = cache.get(nonce)
      val mySender = sender()
      if (maybeF.isDefined)
        cache
          .get(nonce)
          .foreach(_.onComplete {
            case Success(value) => mySender ! Some(value)
            case Failure(_)     => mySender ! None
          })
      else
        mySender ! None
  }
}
