name := "freeacs-http"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-caching" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-xml" % "10.1.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.11",
  "ch.qos.logback" % "logback-classic" % "1.2.1"
)
