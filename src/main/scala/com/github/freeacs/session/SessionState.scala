package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.state.State

final case class SessionState(
    user: String,
    modified: Long,
    state: State,
    errorCount: Int = 0
) extends ReplicatedData {
  type T = SessionState
  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this
}
