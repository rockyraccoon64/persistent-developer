package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, BeforeAndAfterEach}
import rr64.developer.domain.task.TaskInfo
import rr64.developer.infrastructure.PostgresSpec
import rr64.developer.infrastructure.task.TaskTestFacade._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

/**
 * Тесты репозитория задач на основе PostgreSQL + Slick
 */
class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with BeforeAndAfterEach
    with Matchers {

  private val repository = createTaskSlickRepository(database)

  private val saveTasks = saveTasksToRepositoryInSequence(repository) _
  private val findTask = findTaskInRepository(repository) _
  private val listTasks = listTasksFromRepository(repository) _
  private val assertSaved = assertTaskExistsInRepository(repository) _
  private val saveAndAssert = saveTaskToRepositoryAndAssertSaved(repository) _

  private val queuedTask = createTaskInfo(
    id = "30dbff1f-88dc-4972-aa70-a057bf5f1c88",
    difficulty = 5,
    status = queuedTaskStatus
  )

  private val taskInProgress = createTaskInfo(
    id = "959c3bee-9f0b-472e-b45b-1285aa78f215",
    difficulty = 38,
    status = inProgressTaskStatus
  )

  private val finishedTask = createTaskInfo(
    id = "cc972e84-c43a-49dc-8ab2-3a2a36676ac8",
    difficulty = 100,
    status = finishedTaskStatus
  )

  private val taskList = Seq(queuedTask, finishedTask, taskInProgress)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    runQuery {
      sqlu"CREATE TYPE task_status AS ENUM ('Queued', 'InProgress', 'Finished')"
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    runQuery {
      sqlu"""CREATE TABLE task(
           uuid UUID PRIMARY KEY,
           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
           difficulty INT NOT NULL,
           status task_status NOT NULL
         )"""
    }
  }

  override protected def afterEach(): Unit = {
    runQuery {
      sqlu"""DROP TABLE task"""
    }
    super.afterEach()
  }

  /** Тестирование запроса списка задач */
  private def listQueryTest(
    limit: Int,
    offset: Int,
    initial: Seq[TaskInfo],
    expected: Seq[TaskInfo]
  ): Future[Assertion] =
    for {
      _ <- saveTasks(initial)
      list <- listTasks(limit, offset)
    } yield {
      list should contain theSameElementsInOrderAs expected
    }

  /** Репозиторий должен сохранять задачи со статусом "В очереди" */
  "The repository" should "save queued tasks" in
    saveAndAssert(queuedTask)

  /** Репозиторий должен сохранять задачи со статусом "В разработке" */
  "The repository" should "save tasks in progress" in
    saveAndAssert(taskInProgress)

  /** Репозиторий должен сохранять задачи со статусом "Завершено" */
  "The repository" should "save finished tasks" in
    saveAndAssert(finishedTask)

  /** Репозиторий должен обновлять статус у существующих задач */
  "The repository" should "update existing tasks' status" in {
    val initialTask = createTaskInfo(
      id = "a67fb9da-9c25-4bce-ac57-abe4de23f208",
      difficulty = 50,
      status = inProgressTaskStatus
    )
    val updatedTask = initialTask.withStatus(finishedTaskStatus)
    for {
      _ <- saveTasks(initialTask :: updatedTask :: Nil)
      succeeded <- assertSaved(updatedTask)
    } yield succeeded
  }

  /** Репозиторий не должен обновлять сложность у существующих задач */
  "The repository" should "not update existing tasks' difficulty" in {
    val initialTask = createTaskInfo(
      id = "8d22593a-f477-48a2-be4a-79f2d8e34f91",
      difficulty = 15,
      status = inProgressTaskStatus
    )
    val updatedTask = initialTask.withDifficulty(1)
    for {
      _ <- saveTasks(initialTask :: updatedTask :: Nil)
      succeeded <- assertSaved(initialTask)
    } yield succeeded
  }

  /** Репозиторий не должен находить несуществующие задачи */
  "The repository" should "not find nonexistent tasks" in {
    val nonexistentId = UUID.fromString("6b8a92d0-f331-410e-bd28-8c23f00ef285")
    for {
      taskOpt <- findTask(nonexistentId)
    } yield taskOpt shouldEqual None
  }

  /** Репозиторий должен возвращать задачи в обратном порядке их создания */
  "The repository" should "list tasks ordered by descending creation date" in
    listQueryTest(
      limit = taskList.size,
      offset = 0,
      initial = taskList,
      expected = taskList.reverse
    )

  /** Репозиторий должен ограничить количество возвращаемых задач переданным в limit числом */
  "The repository" should "limit the number of returned tasks" in
    listQueryTest(
      limit = 2,
      offset = 0,
      initial = taskList,
      expected = taskList.reverse.take(2)
    )

  /** Если задач меньше, чем limit, возвращаются все задачи */
  "The repository" should "return all tasks if the limit exceeds their amount" in
    listQueryTest(
      limit = 4,
      offset = 0,
      initial = taskList,
      expected = taskList.reverse
    )

  /** Репозиторий должен возвращать задачи, начиная с переданного offset */
  "The repository" should "return tasks starting with the given offset" in
    listQueryTest(
      limit = 3,
      offset = 1,
      initial = taskList,
      expected = taskList.reverse.tail
    )

  /** Репозиторий должен учитывать как limit, так и offset */
  "The repository" should "return tasks starting with the given offset and limit their amount" in
    listQueryTest(
      limit = 1,
      offset = 1,
      initial = taskList,
      expected = taskList.reverse.slice(1, 2)
    )

  /** Если offset выходит за пределы количества имеющихся задач, возвращается пустой список */
  "The repository" should "return an empty list when the offset exceeds the amount of tasks" in
    listQueryTest(
      limit = 5,
      offset = 3,
      initial = taskList,
      expected = Nil
    )

  /** Если задач нет, возвращается пустой список */
  "The repository" should "return an empty list when there are no tasks" in
    listQueryTest(
      limit = 10,
      offset = 0,
      initial = Nil,
      expected = Nil
    )

}
