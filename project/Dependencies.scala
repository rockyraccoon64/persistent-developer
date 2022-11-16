import sbt._

object Dependencies {

  object Versions {
    val Scalactic = "3.2.14"
    val Scalatest = "3.2.14"
    val Akka = "2.7.0"
    val Logback = "1.2.11"
    val AkkaPersistenceJdbc = "5.2.0"
    val Slick = "3.4.1"
  }

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % Versions.Scalactic
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % Versions.Scalatest

  val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor-typed" % Versions.Akka
  val akkaPersistence: ModuleID = "com.typesafe.akka" %% "akka-persistence-typed" % Versions.Akka
  val akkaPersistenceJdbc = "com.lightbend.akka" %% "akka-persistence-jdbc" % Versions.AkkaPersistenceJdbc
  val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query" % Versions.Akka
  val akkaPersistenceTestkit: ModuleID = "com.typesafe.akka" %% "akka-persistence-testkit" % Versions.Akka

  val slick = "com.typesafe.slick" %% "slick" % Versions.Slick
  val slickHikariCp = "com.typesafe.slick" %% "slick-hikaricp" % Versions.Slick

  val logback: ModuleID = "ch.qos.logback" % "logback-classic" % Versions.Logback

}
