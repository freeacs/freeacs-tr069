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

class Tr069Routes(cb: CircuitBreaker, services: Tr069Services) extends Directives with Marshallers {

  def routes(implicit mat: Materializer, ec: ExecutionContext): Route =
    logRequestResult("tr069") {
      (post & entity(as[SOAPRequest])) { soapRequest =>
        path("tr069") {
          complete(cb.withCircuitBreaker(handle(soapRequest)))
        }
      }
    }

  def handle(soapRequest: SOAPRequest)(implicit ec: ExecutionContext): Future[ToResponseMarshallable] = {
    soapRequest match {
      case _: InformRequest =>
        services.unitTypeRepository.list()
          .map(list => {
            println(list)
            InformResponse()
          })
      case UnknownRequest(Empty) =>
        Future.successful(OK)
      case _ =>
        Future.successful(NotImplemented)
    }
  }

}
