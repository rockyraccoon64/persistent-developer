package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, BeforeAndAfterEach}
import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.PostgresSpec
import rr64.developer.infrastructure.task.query.LimitOffsetQueryFactoryImpl
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

  private val statusCodec = new TaskStatusCodec
  private val repository = new TaskSlickRepository(database, statusCodec)
  private val queryFactory = new LimitOffsetQueryFactoryImpl(defaultLimit = 20, maxLimit = 100)

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

  /** Сохранить задачу и запросом проверить, что она сохранена */
  private def saveAndAssert(task: TaskInfo): Future[Assertion] =
    for {
      _ <- repository.save(task)
      succeeded <- assertSaved(task)
    } yield succeeded

  /** Проверить, что задача сохранена в репозитории */
  private def assertSaved(task: TaskInfo): Future[Assertion] =
    for (taskOpt <- repository.findById(task.id)) yield taskOpt shouldEqual Some(task)

  /** Тестирование запроса списка задач */
  private def listQueryTest(
    limit: Int,
    offset: Int,
    initial: Seq[TaskInfo],
    expected: Seq[TaskInfo]
  ): Future[Assertion] = {
    val query = queryFactory.create(limit, offset)
    for {
      _ <- initial.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
        acc.flatMap(_ => repository.save(task))
      }
      list <- repository.list(query)
    } yield {
      list should contain theSameElementsInOrderAs expected
    }
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
