import Dependencies._

organization := "rr64"
name := "persistent-developer"
version := "0.1"
scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  akkaActor,
  akkaPersistence,
  akkaPersistenceJdbc,
  akkaPersistenceQuery,
  slick,
  slickHikariCp,
  scalactic,
  logback,
  akkaPersistenceTestkit % Test,
  scalatest % Test
)