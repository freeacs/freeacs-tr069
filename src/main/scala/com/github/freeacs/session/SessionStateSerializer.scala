package com.github.freeacs.session

import akka.actor.ExtendedActorSystem
import akka.cluster.ddata.protobuf.SerializationSupport
import akka.serialization.Serializer
import com.github.freeacs.session.sessionState.SessionState

class SessionStateSerializer(val system: ExtendedActorSystem)
    extends Serializer
    with SerializationSupport {

  override def includeManifest: Boolean = false

  override def identifier: Int = 99999

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case s: SessionState => s.toByteArray
    case _ ⇒
      throw new IllegalArgumentException(
        s"Can't serialize object of type ${obj.getClass}"
      )
  }

  override def fromBinary(
      bytes: Array[Byte],
      manifest: Option[Class[_]]
  ): AnyRef = SessionState.parseFrom(bytes)
}
