package rr64.developer.infrastructure

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Suite}
import rr64.developer.infrastructure.PostgresSpec._
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
    s"jdbc:postgresql://$host:$port/$defaultDatabase",
    user = user,
    password = password,
    driver = driver
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(postgres.run {
      sqlu"DROP DATABASE IF EXISTS #$dbname"
    }, timeout)

    Await.result(postgres.run {
      sqlu"CREATE DATABASE #$dbname"
    }, timeout)
  }

  override protected def afterAll() {
    Await.result(postgres.run {
      sqlu"DROP DATABASE #$dbname"
    }, timeout)
    super.afterAll()
  }

  val database = Database.forURL(
    s"jdbc:postgresql://$host:$port/$dbname",
    user = user,
    password = password,
    driver = driver
  )

}

object PostgresSpec {
  private[this] val config = ConfigFactory.load().getConfig("postgres-test")
  private val host = config.getString("host")
  private val port = config.getInt("port")
  private val defaultDatabase = config.getString("default-db")
  private val user = config.getString("user")
  private val password = config.getString("password")
}
