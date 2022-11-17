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

  /** Состояние разработчика должно добавляться в БД */
  "The free developer state" should "be saved to the database" in {
    val state = ("dev-1", DeveloperState.Free)
    for {
      count <- database.run {
        table += state
      }
      result <- database.run {
        table.filter(_.id === "dev-1").result
      }
    } yield {
      count shouldEqual 1
      result shouldEqual Seq(state)
    }
  }

}
