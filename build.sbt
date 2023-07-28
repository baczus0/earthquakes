ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

import sbt._

lazy val http4sVersion = "0.23.9"
lazy val circeVersion = "0.14.2"
lazy val catsEffectVersion = "3.3.12"
lazy val munitVersion = "1.0.7"
lazy val scalaMockVersion = "5.1.0"
lazy val scalaTestVersion = "3.2.10"
lazy val pureConfigVersion = "0.17.4"
lazy val logbackClassicVersion = "1.4.7"
lazy val scalaLoggingVersion = "3.9.5"

lazy val root = (project in file("."))
  .settings(
    name := "earthquakes",
    idePackagePrefix := Some("earthquakes"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.scalamock" %% "scalamock" % scalaMockVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % munitVersion % Test
    ),
    testFrameworks ++= Seq(new TestFramework("munit.Framework"), new TestFramework("org.scalatest.tools.Framework"))
  )
