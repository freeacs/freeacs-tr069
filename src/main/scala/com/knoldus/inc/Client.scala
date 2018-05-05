package com.knoldus.inc

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.Cache
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.knoldus.inc.routes.UserRoutes
import com.knoldus.inc.shopping.ComputeCart
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

/**
  * *******************Driver Program for Akka-Http Application********************************
  *
  *
  * Akka-Http example demonstrating usage of Cache in Akka-Http
  * 1. We have build cart component of Online Shopping
  * 2. For every product added into the cart we compute total cart value by adding price of
  * all products
  * 3. For every request for total cart value we don't want to compute this Heavy Computation
  * again and again until there are no new products added into the shopping cart
  * 4. We have just taken a simple scenario to describe a heavy computation but in Production
  * it can be call to some un-managed service whose result we want to cache.
  * 5. Since float arithmetic is heavy computation from CPU Perspective so we can use Cache
  * to avoid re-computation of total cart untill there are no new products added
  */
object Client {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    /**
      * 1. Instantiation of Akka-Http Cache, LfuCache
      * 2. By Default Caching Strategy Provided by Akka-Http is Least Frequently Used
      * 3. For customizing additional setting like time to live etc
      * we can use CachingSettings class and configure it for our requirement
      */
    val cache: Cache[String, Float] = LfuCache[String, Float]
    val computeCart = new ComputeCart(cache)
    val userRoutes: UserRoutes = new UserRoutes(cache, computeCart)

    /**
      * Setting up Akka-Http Server and binding the routes
      */
    val config = ConfigFactory.load("settings.properties")
    val hostname = config.getString("http.host")
    val port = config.getInt("http.port")
    val server = Http().bindAndHandle(userRoutes.routes, hostname, port)
    println(s"Listening on $hostname:$port")
    println("Http server started!")
    StdIn.readLine()

    server.flatMap(_.unbind)
    system.terminate()
    println("Http server terminated!")
  }
}
