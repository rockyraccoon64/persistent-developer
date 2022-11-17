package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with Matchers {

  private val repository = new TaskSlickRepository(database)

  private val queuedTask = TaskInfo(
    id = UUID.fromString("30dbff1f-88dc-4972-aa70-a057bf5f1c88"),
    difficulty = 5,
    status = TaskStatus.Queued
  )

  private val taskInProgress = TaskInfo(
    id = UUID.fromString("959c3bee-9f0b-472e-b45b-1285aa78f215"),
    difficulty = 38,
    status = TaskStatus.InProgress
  )

  private val finishedTask = TaskInfo(
    id = UUID.fromString("cc972e84-c43a-49dc-8ab2-3a2a36676ac8"),
    difficulty = 100,
    status = TaskStatus.InProgress
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

  private def saveAndAssert(task: TaskInfo): Future[Assertion] =
    for {
      _ <- repository.save(task)
      result <- repository.findById(task.id)
    } yield {
      result shouldEqual Some(task)
    }

  private def assertSaved(task: TaskInfo): Future[Assertion] =
    for (taskOpt <- repository.findById(task.id)) yield taskOpt shouldEqual Some(task)

  /** Репозиторий должен сохранять задачи со статусом "В очереди" */
  "The repository" should "save queued tasks" in {
    saveAndAssert(queuedTask)
  }

  /** Репозиторий должен сохранять задачи со статусом "В разработке" */
  "The repository" should "save tasks in progress" in {
    saveAndAssert(taskInProgress)
  }

  /** Репозиторий должен сохранять задачи со статусом "Завершено" */
  "The repository" should "save finished tasks" in {
    saveAndAssert(finishedTask)
  }

  /** Репозиторий должен обновлять статус у существующих задач */
  "The repository" should "update existing tasks' status" in {
    val initialTask = TaskInfo(
      id = UUID.fromString("a67fb9da-9c25-4bce-ac57-abe4de23f208"),
      difficulty = 50,
      status = TaskStatus.InProgress
    )
    val updatedTask = initialTask.copy(status = TaskStatus.Finished)
    for {
      _ <- repository.save(initialTask)
      _ <- saveAndAssert(updatedTask)
    } yield succeed
  }

  /** Репозиторий не должен обновлять сложность у существующих задач */
  "The repository" should "not update existing tasks' difficulty" in {
    val initialTask = TaskInfo(
      id = UUID.fromString("8d22593a-f477-48a2-be4a-79f2d8e34f91"),
      difficulty = 15,
      status = TaskStatus.InProgress
    )
    val updatedTask = initialTask.copy(difficulty = 1)
    for {
      _ <- repository.save(initialTask)
      _ <- repository.save(updatedTask)
      succeeded <- assertSaved(initialTask)
    } yield succeeded
  }

  /** Репозиторий должен находить существующие задачи */

  /** Репозиторий должен не должен находить несуществующие задачи */

  /** Репозиторий должен возвращать список всех задач */

}
