package com.github.freeacs.session
import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.state.FSM

final case class SessionState(
    username: String,
    lastModified: Long,
    fsm: FSM
) extends ReplicatedData {
  type T = SessionState
  def merge(that: SessionState): SessionState =
    if (that.lastModified > this.lastModified) that else this
}
