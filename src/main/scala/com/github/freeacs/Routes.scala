package com.github.freeacs

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.github.freeacs.actors.ConversationActor
import com.github.freeacs.services.{AuthenticationService, Tr069Services}
import com.github.freeacs.xml._
import com.github.freeacs.xml.marshaller.Marshallers._

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

class Routes(breaker: CircuitBreaker,
             services: Tr069Services,
             authService: AuthenticationService,
             responseTimeout: FiniteDuration,
             actorTimeout: FiniteDuration)
            (implicit mat: Materializer, system: ActorSystem, ec: ExecutionContext)
  extends Directives {

  def routes: Route =
    post {
      path("tr069") {
        authenticateConversation((u, p) =>
          entity(as[SOAPRequest]) { soapRequest =>
            complete(handle(soapRequest, u, p))
          })
      }
    }

  def authenticateConversation(route: (String, String) => Route) = {
    extractCredentials {
      case Some(credentials) =>
        credentials.scheme() match {
          case "Basic" =>
            val (username, password) = decodeBasicAuth(credentials.token())
            authenticate(username, password, route)
          case "Digest" =>
            val username = credentials.params("username")
            val password = credentials.params("response")
            // TODO make shit work
            authenticate(username, password, route)
        }
      case None =>
        complete(unauthorized)
    }
  }

  def authenticate(username: String, password: String, route: (String, String) => Route) = {
    onComplete(authService.authenticator(username, password)) {
      case Success(Right(_)) =>
        route(username, password)
      case Success(Left(_)) =>
        complete(unauthorized)
      case Failure(_) =>
        complete(StatusCodes.InternalServerError)
    }
  }

  def decodeBasicAuth(authHeader: String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
    val decoded = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }

  def unauthorized =
    HttpResponse(
      status = StatusCodes.Unauthorized,
      headers = immutable.Seq(RawHeader("WWW-Authenticate", "Basic realm=\"auth@freeacs.com\""))
    )

  def handle(soapRequest: SOAPRequest, user: String, pass: String): Future[ToResponseMarshallable] = {
    implicit val timeout: Timeout = responseTimeout
    breaker.withCircuitBreaker(
      getConversationActor(user).flatMap(_ ? soapRequest)
    ).map(_.asInstanceOf[SOAPResponse])
      .map[ToResponseMarshallable] { response =>
      Marshal(response).to[Either[SOAPResponse, NodeSeq]].map {
        case Right(elm) =>
          makeHttpResponse(StatusCodes.OK, MediaTypes.`text/xml`, Some(elm.toString()))
        case Left(InvalidRequest) =>
          makeHttpResponse(StatusCodes.BadRequest, MediaTypes.`text/plain`, Some("Invalid request"))
        case _ =>
          makeHttpResponse(StatusCodes.OK, MediaTypes.`text/plain`, None)
      }
    }.recover {
      case _: CircuitBreakerOpenException =>
        Future.successful(HttpResponse(StatusCodes.TooManyRequests).withEntity("Server Busy"))
      case e: Throwable =>
        Future.successful(HttpResponse(StatusCodes.InternalServerError))
    }
  }

  def makeHttpResponse(status: StatusCode, charset: MediaType.WithOpenCharset, str: Option[String]) = {
    HttpResponse(
      status = status,
      entity = HttpEntity.CloseDelimited(
        ContentType.WithCharset(charset, HttpCharsets.`UTF-8`),
        Source.single(str.map(ByteString.apply).getOrElse(ByteString.empty))
      )
    )
  }

  def getConversationActor(user: String): Future[ActorRef] = {
    val actorName = s"conversation-$user"
    val actorProps = ConversationActor.props(user, services)
    system.actorSelection(s"user/$actorName")
      .resolveOne(actorTimeout)
      .recover { case _: Exception =>
        system.actorOf(actorProps, actorName)
      }
  }

}
