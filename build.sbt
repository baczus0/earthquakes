ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

import sbt._

lazy val http4sVersion = "0.23.9"
lazy val circeVersion = "0.14.2"
lazy val catsEffectVersion = "3.3.12"
lazy val munitVersion = "1.0.7"

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
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.typelevel" %% "munit-cats-effect-3" % munitVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
