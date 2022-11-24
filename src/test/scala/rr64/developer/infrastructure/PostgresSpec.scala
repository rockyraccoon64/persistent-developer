package rr64.developer.infrastructure

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Suite}
import rr64.developer.infrastructure.PostgresSpec._
import slick.dbio.{DBIOAction, NoStream}
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

  /** БД для использования в тестах */
  val database = Database.forURL(
    s"jdbc:postgresql://$host:$port/$dbname",
    user = user,
    password = password,
    driver = driver
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    runPrepQuery(sqlu"DROP DATABASE IF EXISTS #$dbname")
    runPrepQuery(sqlu"CREATE DATABASE #$dbname")
  }

  override protected def afterAll() {
    runPrepQuery(sqlu"DROP DATABASE #$dbname")
    super.afterAll()
  }

  /** Выполнить запрос в тестовой базе данных */
  protected def runQuery[R](query: DBIOAction[R, NoStream, Nothing]): R =
    runImpl(query, database)

  /** Выполнить запрос для подготовки тестовой базы данных */
  private def runPrepQuery[R](query: DBIOAction[R, NoStream, Nothing]): R =
    runImpl(query, postgres)

  /** Выполнить запрос в базе данных */
  private def runImpl[R](query: DBIOAction[R, NoStream, Nothing], database: Database): R =
    Await.result(database.run(query), timeout)

}

object PostgresSpec {
  private[this] val config = ConfigFactory.load().getConfig("postgres-test")
  private val host = config.getString("host")
  private val port = config.getInt("port")
  private val defaultDatabase = config.getString("default-db")
  private val user = config.getString("user")
  private val password = config.getString("password")
}
