package rr64.developer.infrastructure.facade.task

import rr64.developer.domain.task.TaskInfo
import rr64.developer.infrastructure.task._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

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