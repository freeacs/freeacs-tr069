package com.github.freeacs.actors

import com.github.freeacs.domain
import com.github.freeacs.domain.UnitType
import com.github.freeacs.xml.{SOAPRequest, SOAPResponse}

case class ConversationData(
    startTimestamp: Long = System.currentTimeMillis(),
    history: List[(SOAPRequest, SOAPResponse)] = List(),
    unit: Option[domain.Unit] = None,
    unitType: Option[UnitType] = None,
    exception: Option[Throwable] = None,
    nc: Option[String] = None
)
