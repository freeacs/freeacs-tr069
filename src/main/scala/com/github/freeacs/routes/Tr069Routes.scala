package com.github.freeacs.routes

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import com.github.freeacs.entities.SOAPMethod._
import com.github.freeacs.entities.{InformRequest, InformResponse, SOAPRequest, UnknownRequest}
import com.github.freeacs.marshaller.Marshallers
import com.github.freeacs.services.Tr069Services

import scala.concurrent.{ExecutionContext, Future}

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services)
                 (implicit mat: Materializer, ec: ExecutionContext) extends Directives with Marshallers {

  def routes: Route =
    logRequestResult("tr069") {
      (post & entity(as[SOAPRequest])) { soapRequest =>
        path("tr069") {
          complete(cb.withCircuitBreaker(handle(soapRequest)))
        }
      }
    }

  def handle(soapRequest: SOAPRequest): Future[ToResponseMarshallable] = {
    soapRequest match {
      case inR: InformRequest =>
        for {
          unitTypes <- services.unitTypeRepository.list()
          profiles <- services.profileRepository.list()
          units <- services.unitRepository.list()
        } yield {
          println(unitTypes, profiles, units)
          println(inR)
          InformResponse()
        }
      case UnknownRequest(Empty) =>
        Future.successful(OK)
      case _ =>
        Future.successful(NotImplemented)
    }
  }

}
