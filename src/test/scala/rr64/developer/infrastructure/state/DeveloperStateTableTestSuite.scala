package rr64.developer.infrastructure.state

import org.scalatest.flatspec.{AnyFlatSpecLike, AsyncFlatSpecLike}
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

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

  /** Состояние "Свободен" должно добавляться в БД */
  "The free developer state" should "be saved to the database" in {
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
}
