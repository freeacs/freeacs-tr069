name := "freeacs-http"

version := "0.1"

scalaVersion := "2.12.6"

enablePlugins(JavaAppPackaging)
enablePlugins(ScalaxbPlugin)

lazy val akkaHttpV = "10.1.3"
lazy val akkaV = "2.5.14"

scalaxbPackageName in (Compile, scalaxb) := "generated"
scalaxbAutoPackages in (Compile, scalaxb) := true
scalaxbDispatchVersion in (Compile, scalaxb) := "0.13.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-caching" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "com.h2database" % "h2" % "1.4.197"
)
