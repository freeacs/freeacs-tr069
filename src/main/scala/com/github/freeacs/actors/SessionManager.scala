package com.github.freeacs.actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, ORSet, ORSetKey}

// Distributed Data
// CRDT(Conflict-free replicated data type)

case class SessionId(value: UUID = UUID.randomUUID())

object SessionManager {

  case class CreateSession(sessionId: SessionId)
  case class SessionCreated(sessionId: SessionId)

  case class TerminateSession(sessionId: SessionId)
  case class SessionTerminated(sessionId: SessionId)

  case object GetSessionIds
  case class SessionIds(ids: Set[SessionId])

  def props() = Props(new SessionManager)

}

class SessionManager extends Actor {
  import SessionManager._

  private val replicator       = DistributedData(context.system).replicator
  private val sessionIdsKey    = ORSetKey[SessionId]("SessionIds")
  implicit private val cluster = Cluster(context.system)

  override def receive: Receive = {

    case CreateSession(id) =>
      replicator ! Update(
        sessionIdsKey,
        ORSet.empty[SessionId],
        WriteLocal,
        request = Some(sender() -> SessionCreated(id))
      ) { existingIds =>
        existingIds + id
      }

    case UpdateSuccess(
        `sessionIdsKey`,
        Some((originalSender: ActorRef, response: SessionCreated))
        ) =>
      originalSender ! response

    case TerminateSession(id) =>
      replicator ! Update(
        sessionIdsKey,
        ORSet.empty[SessionId],
        WriteLocal,
        request = Some(sender() -> SessionTerminated(id))
      ) { existingIds =>
        existingIds - id
      }

    case UpdateSuccess(
        `sessionIdsKey`,
        Some((originalSender: ActorRef, response: SessionTerminated))
        ) =>
      originalSender ! response

    case GetSessionIds =>
      replicator ! Get(sessionIdsKey, ReadLocal, request = Some(sender()))

    case result @ GetSuccess(`sessionIdsKey`, Some(originalSender: ActorRef)) =>
      originalSender ! SessionIds(result.get(sessionIdsKey).elements)

  }

}
