package com.github.freeacs.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.util.Timeout
import com.github.freeacs.config.Configuration
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._

import scala.concurrent.ExecutionContext

case class SessionState(
    username: String,
    created: Long,
    nonce: Option[String]
) extends ReplicatedData {
  type T = SessionState
  def merge(that: SessionState): SessionState = {
    val username = Option(this.username).getOrElse(that.username)
    val created  = Option(this.created).getOrElse(that.created)
    val nonce    = this.nonce.orElse(that.nonce)
    SessionState(username, created, nonce)
  }
}

case class NonceState(created: Long) extends ReplicatedData {
  type T = NonceState
  def merge(that: NonceState): NonceState =
    NonceState(created = Option(this.created).getOrElse(that.created))
}

object Conversation {
  case class CreateSessionIfNotPresent(username: String)

  case class SetNonce(
      created: Long = System.currentTimeMillis(),
      nonce: String
  )

  case class GetNonceCreated(username: String)

  case class GetResponse(
      username: String,
      request: SOAPRequest
  )

  def props(services: Tr069Services, config: Configuration) =
    Props(new Conversation(services, config))

}

class Conversation(services: Tr069Services, config: Configuration)
    extends Actor
    with ActorLogging {
  import Conversation._

  private val replicator            = DistributedData(context.system).replicator
  implicit private val cluster      = Cluster(context.system)
  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val as: ActorSystem      = context.system
  implicit val timeout: Timeout     = config.responseTimeout
  private val conversations         = ORMapKey[String, SessionState]("Conversations")
  private val nonces                = ORMapKey[String, NonceState]("Nonces")

  override def receive: Receive = {

    case get @ GetResponse(user, in: InformRequest) =>
      val response = InformResponse()
      log.info("Got INReq. Returning INRes")
      sender ! response

    case get @ GetResponse(user, EmptyRequest) =>
      val response = GetParameterNamesRequest("InternetGatewayDevice.")
      log.info("Got EM. Returning GPNReq")
      sender ! response

    case get @ GetResponse(user, gpnr: GetParameterNamesResponse) =>
      val response = SetParameterValuesRequest()
      log.info("Got GPNRes. Returning SPVReq")
      sender ! response

    case get @ GetResponse(user, spvr: SetParameterValuesResponse) =>
      val response = EmptyResponse
      log.info("Got SPVRes. Returning EM")
      sender ! response

    case CreateSessionIfNotPresent(id) =>
      replicator ! Update(
        conversations,
        ORMap.empty[String, SessionState],
        WriteLocal,
        request = None
      ) { existingIds =>
        if (!existingIds.contains(id)) {
          existingIds + (id -> SessionState(
            id,
            System.currentTimeMillis(),
            None
          ))
        } else
          existingIds
      }

    case SetNonce(created, nonce) =>
      replicator ! Update(
        nonces,
        ORMap.empty[String, NonceState],
        WriteLocal,
        request = None
      ) { existingIds =>
        existingIds + (nonce -> NonceState(created))
      }

    case GetNonceCreated(nonce) =>
      replicator ! Get(
        nonces,
        ReadLocal,
        request = Some((sender, nonce))
      )

    case result @ GetSuccess(
          `nonces`,
          Some((sender: ActorRef, nonce: String))
        ) =>
      sender ! result.get(nonces).get(nonce).map(_.created)
  }

}
