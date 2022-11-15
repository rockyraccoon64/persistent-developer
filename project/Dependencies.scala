import sbt._

object Dependencies {

  object Versions {
    val Scalactic = "3.2.14"
    val Scalatest = "3.2.14"
    val Akka = "2.7.0"
  }

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % Versions.Scalactic
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % Versions.Scalatest

  val akkaPersistence: ModuleID = "com.typesafe.akka" %% "akka-persistence-typed" % Versions.Akka
  val akkaPersistenceTestkit: ModuleID = "com.typesafe.akka" %% "akka-persistence-testkit" % Versions.Akka

}
