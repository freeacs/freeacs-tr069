package com.knoldus.inc.entities

import play.api.libs.json.{Json, OFormat}

case class Product(productId: String, productName: String, productDescription: String, productPrice: Float,
                   brandName: String, expiryDate: String)

object Product {
  implicit val Product: OFormat[Product] = Json.format[Product]
}