import Dependencies._

organization := "rr64"
name := "persistent-developer"
version := "0.1"
scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  akkaActor,
  akkaStream,
  akkaHttp,
  akkaHttpSprayJson,
  akkaPersistence,
  akkaPersistenceJdbc,
  akkaPersistenceQuery,
  akkaProjectionEventSourced,
  akkaProjectionJdbc,
  slick,
  slickHikariCp,
  scalactic,
  postgres,
  logback,
  akkaPersistenceTestkit % Test,
  akkaProjectionTestkit % Test,
  akkaStreamTestkit % Test,
  akkaHttpTestkit % Test,
  scalatest % Test,
  scalamock % Test
)