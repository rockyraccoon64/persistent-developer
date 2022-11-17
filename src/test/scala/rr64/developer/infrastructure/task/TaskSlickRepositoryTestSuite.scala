package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.PostgresSpec

import java.util.UUID

class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with Matchers {

  private val queuedTask = TaskInfo(
    id = UUID.fromString("30dbff1f-88dc-4972-aa70-a057bf5f1c88"),
    difficulty = 5,
    status = TaskStatus.Queued
  )

  /** Репозиторий должен сохранять задачи со статусом "В очереди" */
  "The repository" should "save queued tasks" in {
    val repository = new TaskSlickRepository
    for {
      count <- repository.save(queuedTask)
      result <- repository.findById(queuedTask.id)
    } yield {
      count shouldEqual 1
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
