package com.knoldus.inc.routes

import akka.http.caching.scaladsl.Cache
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.knoldus.inc.entities._
import com.knoldus.inc.shopping.ComputeCart
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

/**
  *
  * @param cache
  * @param computeCart
  */

class UserRoutes(cache: Cache[String, Float], computeCart: ComputeCart) extends PlayJsonSupport {

  val routes = computeCartValue ~ addProductInCart

  def computeCartValue: Route =
    get {
      path("cart") {
        complete(computeCart.getCartValue())
      }
    }

  def addProductInCart: Route =
    (post & entity(as[Product])) { product =>
      path("cart") {
        complete(computeCart.addProduct(product))
      }
    }
}
