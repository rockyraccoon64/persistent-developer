package rr64.developer.infrastructure.state

import org.scalatest.flatspec.{AnyFlatSpecLike, AsyncFlatSpecLike}
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

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

  /** Состояние "Свободен" должно добавляться и извлекаться из БД */
  "The free developer state" should "be inserted and retrieved from the database" in {
    val id = "dev-1"
    val state = (id, DeveloperState.Free)
    for {
      count <- database.run {
        table += state
      }
      result <- database.run {
        table.filter(_.id === id).result
      }
    } yield {
      count shouldEqual 1
      result shouldEqual Seq(state)
    }
  }

  /** Состояние "Работает" должно добавляться и извлекаться из БД */
  "The Working developer state" should "be inserted and retrieved from the database" in {
    val id = "dev-2"
    val state = (id, DeveloperState.Working)
    for {
      count <- database.run {
        table += state
      }
      result <- database.run {
        table.filter(_.id === id).result
      }
    } yield {
      count shouldEqual 1
      result shouldEqual Seq(state)
    }
  }

}
