package rr64.developer.infrastructure.facade.task

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.task._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

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

  /** Тестовый репозиторий задач на основе Slick */
  def createTestTaskSlickRepository(database: Database): TestTaskSlickRepository =
    new TestTaskSlickRepository(
      new TaskSlickRepository(database, new TaskStatusCodec)
    )

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