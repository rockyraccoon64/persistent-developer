package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, BeforeAndAfterEach}
import rr64.developer.domain.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.PostgresSpec
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with BeforeAndAfterEach
    with Matchers {

  private val repository = new TaskSlickRepository(database)

  private val queuedTask = TaskInfo(
    id = UUID.fromString("30dbff1f-88dc-4972-aa70-a057bf5f1c88"),
    difficulty = Difficulty(5),
    status = TaskStatus.Queued
  )

  private val taskInProgress = TaskInfo(
    id = UUID.fromString("959c3bee-9f0b-472e-b45b-1285aa78f215"),
    difficulty = Difficulty(38),
    status = TaskStatus.InProgress
  )

  private val finishedTask = TaskInfo(
    id = UUID.fromString("cc972e84-c43a-49dc-8ab2-3a2a36676ac8"),
    difficulty = Difficulty(100),
    status = TaskStatus.InProgress
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(
      database.run {
        sqlu"""CREATE TABLE task(
             serial_id SERIAL PRIMARY KEY,
             uuid UUID NOT NULL UNIQUE,
             difficulty INT NOT NULL,
             status VARCHAR(10) NOT NULL
           )"""
      }, 10.seconds
    )
  }

  override protected def afterEach(): Unit = {
    Await.result(
      database.run {
        sqlu"""DROP TABLE task"""
      }, 10.seconds
    )
    super.afterEach()
  }

  private def saveAndAssert(task: TaskInfo): Future[Assertion] =
    for {
      _ <- repository.save(task)
      succeeded <- assertSaved(task)
    } yield succeeded

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
      difficulty = Difficulty(50),
      status = TaskStatus.InProgress
    )
    val updatedTask = initialTask.copy(status = TaskStatus.Finished)
    for {
      _ <- repository.save(initialTask)
      succeeded <- saveAndAssert(updatedTask)
    } yield succeeded
  }

  /** Репозиторий не должен обновлять сложность у существующих задач */
  "The repository" should "not update existing tasks' difficulty" in {
    val initialTask = TaskInfo(
      id = UUID.fromString("8d22593a-f477-48a2-be4a-79f2d8e34f91"),
      difficulty = Difficulty(15),
      status = TaskStatus.InProgress
    )
    val updatedTask = initialTask.copy(difficulty = Difficulty(1))
    for {
      _ <- repository.save(initialTask)
      _ <- repository.save(updatedTask)
      succeeded <- assertSaved(initialTask)
    } yield succeeded
  }

  /** Репозиторий не должен находить несуществующие задачи */
  "The repository" should "not find nonexistent tasks" in {
    val nonexistentId = UUID.fromString("6b8a92d0-f331-410e-bd28-8c23f00ef285")
    for {
      taskOpt <- repository.findById(nonexistentId)
    } yield taskOpt shouldEqual None
  }

  /** Репозиторий должен возвращать список всех задач */
  "The repository" should "list all tasks" in {
    val tasks = Seq(queuedTask, finishedTask, taskInProgress)
    for {
      _ <- Future.traverse(tasks)(repository.save)
      list <- repository.list(LimitOffsetQuery.Default)
    } yield {
      list should contain theSameElementsAs tasks
    }
  }

  /** Репозиторий должен ограничить количество возвращаемых задач переданным в limit числом */
  "The repository" should "limit the number of returned tasks" in {
    val tasks = Seq(queuedTask, finishedTask, taskInProgress)
    val expected = tasks.take(2)
    val query = LimitOffsetQuery(
      limit = 2,
      offset = 0
    )
    for {
      _ <- tasks.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
        acc.flatMap(_ => repository.save(task))
      }
      list <- repository.list(query)
    } yield {
      list should contain theSameElementsInOrderAs expected
    }
  }

  /** Если задач меньше, чем limit, возвращаются все задачи */
  "The repository" should "return all tasks if the limit exceeds their amount" in {
    val tasks = Seq(queuedTask, finishedTask, taskInProgress)
    val query = LimitOffsetQuery(
      limit = 4,
      offset = 0
    )
    for {
      _ <- tasks.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
        acc.flatMap(_ => repository.save(task))
      }
      list <- repository.list(query)
    } yield {
      list should contain theSameElementsInOrderAs tasks
    }
  }

}
