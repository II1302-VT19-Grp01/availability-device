name := """dir"""
organization := "come.II1302"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies +=  "com.ibm.messaging" % "watson-iot" %  "0.2.6"
libraryDependencies += "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.2.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test

ensimeScalaVersion in ThisBuild := "2.12.8"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "come.II1302.controllers._"

ensimeServerVersion in ThisBuild := "2.0.2"
