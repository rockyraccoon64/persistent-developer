package rr64.developer.infrastructure.dev

import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * Тесты репозитория состояний разработчиков на основе PostgreSQL + Slick
 * */
class DeveloperStateSlickRepositoryTestSuite extends PostgresSpec with AsyncFlatSpecLike with Matchers {

  private val codec = new DeveloperStateCodec
  private val repository = new DeveloperStateSlickRepository(database, codec)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(database.run {
      sqlu"""CREATE TABLE dev_state(
            id VARCHAR(100) PRIMARY KEY,
            state VARCHAR(10) NOT NULL
          )"""
    }, 10.seconds)
  }

  private def checkInsert(id: String, state: DeveloperState): Future[Assertion] =
    for {
      _ <- repository.save(id, state)
      stateResult <- repository.findById(id)
    } yield {
      stateResult shouldEqual Some(state)
    }

  /** Состояние "Свободен" должно добавляться и извлекаться из репозитория */
  "The free developer state" should "be inserted and retrieved from the repository" in {
    checkInsert("dev-1", DeveloperState.Free)
  }

  /** Состояние "Работает" должно добавляться и извлекаться из репозитория */
  "The Working developer state" should "be inserted and retrieved from the repository" in {
    checkInsert("dev-2", DeveloperState.Working)
  }

  /** Состояние "Отдыхает" должно добавляться и извлекаться из репозитория */
  "The Resting developer state" should "be inserted and retrieved from the repository" in {
    checkInsert("dev-3", DeveloperState.Resting)
  }

  /** Состояние разработчика должно обновляться */
  "The developer state" should "be updated in the repository" in {
    val id = "dev-4"
    for {
      _ <- repository.save(id, DeveloperState.Working)
      succeeded <- checkInsert(id, DeveloperState.Resting)
    } yield succeeded
  }

  /** Если для разработчика в репозитории нет состояния, ничего не возвращается */
  "The repository" should "not return a state for a given developer if there is none" in {
    repository.findById("nonexistent").map(_ shouldEqual None)
  }

}
