name := "freeacs-http"

version := "0.1"

scalaVersion := "2.12.6"

val akkaHttpV = "10.1.3"
val akkaV = "2.5.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-caching" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
