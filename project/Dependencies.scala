import sbt._

object Dependencies {

  object Versions {
    val Akka = "2.7.0"
    val AkkaHttp = "10.4.0"
    val AkkaProjection = "1.3.0"
    val AkkaPersistenceJdbc = "5.2.0"
    val Slick = "3.4.1"
    val Postgres = "42.5.0"
    val Logback = "1.2.11"
    val Scalactic = "3.2.14"
    val Scalatest = "3.2.14"
    val Scalamock = "5.1.0"
  }

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % Versions.Scalactic
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % Versions.Scalatest
  val scalamock: ModuleID = "org.scalamock" %% "scalamock" % Versions.Scalamock

  val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor-typed" % Versions.Akka
  val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % Versions.Akka
  val akkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % Versions.AkkaHttp
  val akkaHttpSprayJson: ModuleID = "com.typesafe.akka" %% "akka-http-spray-json" % Versions.AkkaHttp
  val akkaPersistence: ModuleID = "com.typesafe.akka" %% "akka-persistence-typed" % Versions.Akka
  val akkaPersistenceJdbc: ModuleID = "com.lightbend.akka" %% "akka-persistence-jdbc" % Versions.AkkaPersistenceJdbc
  val akkaPersistenceQuery: ModuleID = "com.typesafe.akka" %% "akka-persistence-query" % Versions.Akka
  val akkaPersistenceTestkit: ModuleID = "com.typesafe.akka" %% "akka-persistence-testkit" % Versions.Akka
  val akkaProjectionEventSourced: ModuleID = "com.lightbend.akka" %% "akka-projection-eventsourced" % Versions.AkkaProjection
  val akkaProjectionJdbc: ModuleID = "com.lightbend.akka" %% "akka-projection-jdbc" % Versions.AkkaProjection
  val akkaProjectionTestkit: ModuleID = "com.lightbend.akka" %% "akka-projection-testkit" % Versions.AkkaProjection
  val akkaStreamTestkit: ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % Versions.Akka
  val akkaHttpTestkit: ModuleID = "com.typesafe.akka" %% "akka-http-testkit" % Versions.AkkaHttp

  val slick: ModuleID = "com.typesafe.slick" %% "slick" % Versions.Slick
  val slickHikariCp: ModuleID = "com.typesafe.slick" %% "slick-hikaricp" % Versions.Slick

  val postgres: ModuleID = "org.postgresql" % "postgresql" % Versions.Postgres

  val logback: ModuleID = "ch.qos.logback" % "logback-classic" % Versions.Logback

}
