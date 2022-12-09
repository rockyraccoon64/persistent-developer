package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.task.query.LimitOffsetQueryTestFacade
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Фасад для тестов с использованием статуса задач
 */
trait TaskStatusTestFacade {

  /** Задача в очереди */
  def queuedTaskStatus: TaskStatus =
    TaskStatus.Queued

  /** Задача в работе */
  def inProgressTaskStatus: TaskStatus =
    TaskStatus.InProgress

  /** Задача завершена */
  def finishedTaskStatus: TaskStatus =
    TaskStatus.Finished

}

/**
 * Фасад для тестов с использованием TaskWithId
 */
trait TaskWithIdTestFacade {

  /** Создать задачу с идентификатором */
  def createTaskWithId(
    difficulty: Int,
    id: String
  ): TaskWithId = TaskWithId(
    id = id,
    difficulty = difficulty
  )

}

/**
 * Фасад для тестов с использованием TaskInfo
 * */
trait TaskInfoTestFacade {

  /** Информация о существующей задаче */
  type TaskInfo = rr64.developer.domain.task.TaskInfo

  /** Создать объект с информацией о существующей задаче */
  def createTaskInfo(
    id: String,
    difficulty: Int,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = UUID.fromString(id),
    difficulty = Difficulty(difficulty),
    status = status
  )

  implicit class TaskInfoTransformers(task: TaskInfo) {

    /** Задача с другой сложностью */
    def withDifficulty(difficulty: Int): TaskInfo =
      task.copy(difficulty = Difficulty(difficulty))

    /** Задача с другим статусом */
    def withStatus(status: TaskStatus): TaskInfo =
      task.copy(status = status)

  }

}

/**
 * Фасад для тестов с использованием репозитория задач
 * */
trait TaskRepositoryTestFacade {

  /** Создать репозиторий задач на основе Slick */
  def createTaskSlickRepository(database: Database): TaskSlickRepository =
    new TaskSlickRepository(database, new TaskStatusCodec)

  /** Простой in-memory репозиторий для задач */
  def simpleTaskRepository: TaskRepository[Any] =
    new TaskRepository[Any] {
      private var tasks: Map[UUID, TaskInfo] = Map.empty
      override def save(taskInfo: TaskInfo): Future[_] = {
        tasks = tasks.updated(taskInfo.id, taskInfo)
        Future.unit
      }
      override def findById(id: UUID)
          (implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
        Future.successful(tasks.get(id))
      override def list(query: Any)
          (implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
        Future.successful(tasks.values.toSeq)
    }

  implicit class TestTaskRepository[Q](repository: TaskRepository[Q]) {

    /** Сохранить задачу в репозитории */
    def saveTaskToRepository(task: TaskInfo): Future[_] =
      repository.save(task)

    /** Сохранить последовательность задач в репозитории */
    def saveTasksToRepositoryInSequence(tasks: Seq[TaskInfo])
        (implicit ec: ExecutionContext): Future[Any] =
      tasks.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
        acc.flatMap(_ => saveTaskToRepository(task))
      }

    /** Найти задачу в репозитории */
    def findTaskInRepository(id: UUID)
        (implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
      repository.findById(id)

    /** Проверить, что задача существует в репозитории */
    def assertTaskExistsInRepository(task: TaskInfo)
        (implicit ec: ExecutionContext): Future[Assertion] =
      for (taskOpt <- findTaskInRepository(task.id)) yield
        taskOpt shouldEqual Some(task)

    /** Сохранить задачу и проверить, что она сохранена */
    def saveTaskToRepositoryAndAssertSaved(task: TaskInfo)
        (implicit ec: ExecutionContext): Future[Assertion] =
      for {
        _ <- saveTaskToRepository(task)
        succeeded <- assertTaskExistsInRepository(task)
      } yield succeeded

  }

  implicit class TestSlickRepository(repository: TaskSlickRepository) {

    /** Получить список задач из репозитория на основе Slick */
    def listTasksFromRepository(limit: Int, offset: Int)
        (implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = {
      val query = LimitOffsetQueryTestFacade.createQuery(limit, offset)
      repository.list(query)
    }

    /** Тестирование запроса списка задач */
    def testListQuery(
      limit: Int,
      offset: Int,
      initial: Seq[TaskInfo],
      expected: Seq[TaskInfo]
    )(implicit ec: ExecutionContext): Future[Assertion] =
      for {
        _ <- repository.saveTasksToRepositoryInSequence(initial)
        list <- listTasksFromRepository(limit, offset)
      } yield {
        list should contain theSameElementsInOrderAs expected
      }

  }

}

/**
 * Фасад для тестов с использованием задач
 * */
trait TaskTestFacade
  extends TaskStatusTestFacade
    with TaskWithIdTestFacade
    with TaskInfoTestFacade
    with TaskRepositoryTestFacade

object TaskTestFacade
  extends TaskTestFacade