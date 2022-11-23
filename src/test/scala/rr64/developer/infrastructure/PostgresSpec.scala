package rr64.developer.infrastructure

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Suite}
import rr64.developer.infrastructure.PostgresSpec._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * Набор тестов с использованием PostgreSQL
 * */
trait PostgresSpec
  extends Suite
    with BeforeAndAfterAll {

  /** Таймаут ожидания ответа при создании/удалении тестовой БД */
  private val timeout = 10.seconds

  /** Название тестовой БД */
  private val dbname =
    getClass.getSimpleName.toLowerCase

  /** JDBC-драйвер PostgreSQL */
  private val driver =
    "org.postgresql.Driver"

  /** База данных по умолчанию для создания тестовой БД */
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

  /** БД для использования в тестах */
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
