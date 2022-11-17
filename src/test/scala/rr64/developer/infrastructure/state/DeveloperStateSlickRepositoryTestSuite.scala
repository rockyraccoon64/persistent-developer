package rr64.developer.infrastructure.state

import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

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

  private def checkInsert(id: String, state: DeveloperState): Future[Assertion] =
    for {
      _ <- repository.save(id, state)
      stateResult <- repository.findById(id)
    } yield {
      stateResult shouldEqual Some(state)
    }

  /** Состояние "Свободен" должно добавляться и извлекаться из репозитория */
  "The free developer state" should "be inserted" in {
    checkInsert("dev-1", DeveloperState.Free)
  }

  /** Состояние "Работает" должно добавляться и извлекаться из репозитория */
  "The Working developer state" should "be inserted" in {
    checkInsert("dev-2", DeveloperState.Working)
  }


}
