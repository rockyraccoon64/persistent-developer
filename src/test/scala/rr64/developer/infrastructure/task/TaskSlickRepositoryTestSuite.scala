package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with Matchers {

  private val queuedTask = TaskInfo(
    id = UUID.fromString("30dbff1f-88dc-4972-aa70-a057bf5f1c88"),
    difficulty = 5,
    status = TaskStatus.Queued
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(
      database.run {
        sqlu"""CREATE TABLE task(
             id UUID PRIMARY KEY,
             difficulty INT NOT NULL,
             status VARCHAR(10) NOT NULL
           )"""
      }, 10.seconds
    )
  }

  /** Репозиторий должен сохранять задачи со статусом "В очереди" */
  "The repository" should "save queued tasks" in {
    val repository = new TaskSlickRepository(database)
    for {
      _ <- repository.save(queuedTask)
      result <- repository.findById(queuedTask.id)
    } yield {
      result shouldEqual Some(queuedTask)
    }
  }

  /** Репозиторий должен сохранять задачи со статусом "В разработке" */

  /** Репозиторий должен сохранять задачи со статусом "Завершено" */

  /** Репозиторий должен обновлять статус у существующих задач */

  /** Репозиторий должен находить существующие задачи */

  /** Репозиторий должен не должен находить несуществующие задачи */

  /** Репозиторий должен возвращать список всех задач */

}
