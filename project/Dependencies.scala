import sbt._

object Dependencies {

  object Versions {
    val Scalactic = "3.2.14"
    val Scalatest = "3.2.14"
    val Akka = "2.7.0"
    val Logback = "1.2.11"
  }

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % Versions.Scalactic
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % Versions.Scalatest

  val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor-typed" % Versions.Akka
  val akkaPersistence: ModuleID = "com.typesafe.akka" %% "akka-persistence-typed" % Versions.Akka
  val akkaPersistenceTestkit: ModuleID = "com.typesafe.akka" %% "akka-persistence-testkit" % Versions.Akka

  val logback: ModuleID = "ch.qos.logback" % "logback-classic" % Versions.Logback

}
