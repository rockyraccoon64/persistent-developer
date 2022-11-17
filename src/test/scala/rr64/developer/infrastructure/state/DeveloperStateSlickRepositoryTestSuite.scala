package rr64.developer.infrastructure.state

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

class DeveloperStateSlickRepositoryTestSuite extends PostgresSpec with AsyncFlatSpecLike with Matchers {

  private val repository = new DeveloperStateSlickRepository(database)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.run {
      sqlu"""CREATE TABLE dev_state(
            id VARCHAR(100) PRIMARY KEY,
            state VARCHAR(10) NOT NULL
          )"""
    }
  }

  /** Состояние "Свободен" должно добавляться и извлекаться из репозитория */
  "The free developer state" should "be inserted" in {
    for {
      _ <- repository.save("dev-1", DeveloperState.Free)
      state <- repository.findById("dev-1")
    } yield {
      state shouldEqual Some(DeveloperState.Free)
    }
  }
}
