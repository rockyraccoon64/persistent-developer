import sbt._

object Dependencies {

  object Versions {
    val Scalactic = "3.2.14"
    val Scalatest = "3.2.14"
  }

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % Versions.Scalactic
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % Versions.Scalatest

}
