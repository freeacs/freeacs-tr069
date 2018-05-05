package com.knoldus.inc.shopping

import akka.http.caching.scaladsl.Cache
import akka.stream.Materializer
import com.knoldus.inc.entities._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Breaks

/**
  *
  * @param cache
  * @param executionContext
  * @param materializer
  */
class ComputeCart(cache: Cache[String, Float])(implicit val executionContext: ExecutionContext, implicit val materializer: Materializer) {

  var productList: ListBuffer[Product] = ListBuffer.empty
  val CART_VALUE: String = "cartValue"

  /**
    * Calculating total cart value from current shopping cart
    *
    * @return
    */
  def calculateTotalCartValue(): Future[Float] = {
    var total: Float = 0
    for (product <- productList) {
      total += product.productPrice
    }
    Future.successful(total)
  }

  /**
    * If total value is present into the cache return it else set Future of Compute login into the cache which
    * will eventually set the value of cache
    *
    * @return
    */
  def getCartValue(): Future[Float] = {
    cache.getOrLoad(CART_VALUE, _ => calculateTotalCartValue())
  }


  /**
    * If product is added into cart refresh the value of key CART_VALUE in cache and update the same
    *
    * @param product
    * @return
    */
  def addProduct(product: Product): ListBuffer[Product] = {
    productList += product
    cache.remove(CART_VALUE)
    cache.getOrLoad(CART_VALUE, _ => calculateTotalCartValue())
    productList
  }

  /**
    * If product is removed from cart refresh the value of key CART_VALUE in cache and update the same
    *
    * @param productId
    */
  def removeProduct(productId: String): Unit = {
    val break = new Breaks
    break.breakable {
      for (product <- productList) {
        if (product.productId.equalsIgnoreCase(productId)) {
          productList -= product
          cache.remove(CART_VALUE)
          cache.getOrLoad(CART_VALUE, _ => calculateTotalCartValue())
          break.break()
        }
      }
    }
  }
}
