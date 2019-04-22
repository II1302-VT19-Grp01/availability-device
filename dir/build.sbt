name := """dir"""
organization := "come.II1302"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"


libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "com.github.tminglei" %% "slick-pg" % "0.15.6",
  "org.postgresql" % "postgresql" % "42.2.0",
  "com.ibm.messaging" % "watson-iot" %  "0.2.6",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.2.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test

)

// Adds additional packages into Twirl
TwirlKeys.templateImports ++= Seq(
  "controllers.Security.UserReqHeader",
  "database._",
  "views._"
)

// Adds additional packages into conf/routes
play.sbt.routes.RoutesKeys.routesImport ++= Seq(
  "database._",
  "utils.Binders._"
)


ensimeScalaVersion in ThisBuild := "2.12.8"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "come.II1302.controllers._"

ensimeServerVersion in ThisBuild := "2.0.2"
