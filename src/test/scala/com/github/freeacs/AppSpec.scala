package com.github.freeacs

import org.scalatest._
import org.scalatest.concurrent._

class AppSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  "The app" should "return index.html on a GET to /" in {

  }
  "The app" should "return 404 on a GET to /foo" in {

  }
}
