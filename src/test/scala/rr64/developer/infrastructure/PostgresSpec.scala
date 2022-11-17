package rr64.developer.infrastructure

import org.scalatest.{BeforeAndAfterAll, Suite}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait PostgresSpec
  extends Suite
    with BeforeAndAfterAll {

  private val timeout = 10.seconds

  private val dbname =
    getClass.getSimpleName.toLowerCase
  private val driver =
    "org.postgresql.Driver"

  private val postgres = Database.forURL(
    "jdbc:postgresql:postgres",
    user = "postgres",
    password = "postgres",
    driver = driver)

  Await.result(postgres.run {
    sqlu"DROP DATABASE IF EXISTS #$dbname"
  }, timeout)

  Await.result(postgres.run {
    sqlu"CREATE DATABASE #$dbname"
  }, timeout)

  override def afterAll() {
    Await.result(postgres.run {
      sqlu"DROP DATABASE #$dbname"
    }, timeout)
  }

  val database = Database.forURL(
    s"jdbc:postgresql:$dbname",
    user = "postgres",
    password = "postgres",
    driver = driver)

}
