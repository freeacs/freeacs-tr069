package com.github.freeacs.session

import akka.cluster.ddata.ReplicatedData
import com.github.freeacs.services.Tr069Services
import com.github.freeacs.xml._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

final case class SessionState(
    user: String,
    modified: Long,
    state: ExpectRequest,
    errorCount: Int = 0
) extends ReplicatedData {

  type T = SessionState

  def merge(that: SessionState): SessionState =
    if (that.modified > this.modified) that else this

  final case class Result(
      response: SOAPResponse,
      state: SessionState
  )

  val log = LoggerFactory.getLogger(getClass)

  def transition(
      services: Tr069Services,
      request: SOAPRequest
  )(implicit ec: ExecutionContext): Future[Result] = (this, request) match {
    case (
        sessionState @ SessionState(_, _, ExpectInformRequest, _),
        request: InformRequest
        ) =>
      log.info("Got INReq. Returning INRes. " + request.toString)
      Future.successful(
        Result(
          InformResponse(),
          sessionState.copy(state = ExpectEmptyRequest)
        )
      )

    case (
        sessionState @ SessionState(_, _, ExpectEmptyRequest, _),
        EmptyRequest
        ) =>
      log.info("Got EM. Returning GPNReq.")
      val response = GetParameterNamesRequest("InternetGatewayDevice.")
      Future.successful(
        Result(
          response,
          sessionState.copy(state = ExpectGetParameterNamesResponse)
        )
      )

    case (
        sessionState @ SessionState(_, _, ExpectGetParameterNamesResponse, _),
        request: GetParameterNamesResponse
        ) =>
      log.info("Got GPNRes. Returning GPVReq. " + request.toString)
      val response =
        GetParameterValuesRequest(
          Seq(("InternetGatewayDevice.ManagementServer.Username"))
        )
      Future.successful(
        Result(
          response,
          sessionState.copy(state = ExpectGetParameterValuesResponse)
        )
      )

    case (
        sessionState @ SessionState(
          _,
          _,
          ExpectGetParameterValuesResponse,
          _
        ),
        request: GetParameterValuesResponse
        ) =>
      log.info("Got GPVRes. Returning SPVReq. " + request.toString)
      val response = SetParameterValuesRequest()
      Future.successful(
        Result(
          response,
          sessionState.copy(state = ExpectSetParameterValuesResponse)
        )
      )

    case (
        sessionState @ SessionState(
          _,
          _,
          ExpectSetParameterValuesResponse,
          _
        ),
        request: SetParameterValuesResponse
        ) =>
      log.info("Got SPVRes. Returning EM. " + request.toString)
      val response = EmptyResponse
      Future.successful(
        Result(
          response,
          sessionState.copy(state = ExpectInformRequest)
        )
      )

    case (sessionState, req) =>
      log.error(s"Got unexpected request $req in state ${sessionState.state}")
      Future.successful(
        Result(
          InvalidRequest,
          if (sessionState.errorCount < 2)
            sessionState.copy(errorCount = (sessionState.errorCount + 1))
          else
            sessionState.copy(errorCount = 0, state = ExpectInformRequest)
        )
      )
  }

}
