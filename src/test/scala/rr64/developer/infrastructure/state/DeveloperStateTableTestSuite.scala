package rr64.developer.infrastructure.state

import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Тесты DAO хранилища состояний разработчиков на PostgreSQL
 */
class DeveloperStateTableTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with Matchers {

  private val table = TableQuery[DeveloperStateTable]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.run {
      table.schema.createIfNotExists
    }
  }

  private def checkInsert(id: String, state: DeveloperState): Future[Assertion] = {
    val stateTuple = (id, state)
    for {
      count <- database.run {
        table += stateTuple
      }
      result <- database.run {
        table.filter(_.id === id).result
      }
    } yield {
      count shouldEqual 1
      result shouldEqual Seq(stateTuple)
    }
  }

  /** Состояние "Свободен" должно добавляться и извлекаться из БД */
  "The free developer state" should "be inserted and retrieved from the database" in {
    checkInsert("dev-1", DeveloperState.Free)
  }

  /** Состояние "Работает" должно добавляться и извлекаться из БД */
  "The Working developer state" should "be inserted and retrieved from the database" in {
    checkInsert("dev-2", DeveloperState.Working)
  }

  /** Состояние "Отдыхает" должно добавляться и извлекаться из БД */
  "The Resting developer state" should "be inserted and retrieved from the database" in {
    checkInsert("dev-3", DeveloperState.Resting)
  }

}
