package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.task.query.LimitOffsetQueryTestFacade
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait TaskTestFacade {

  type TaskInfo = rr64.developer.domain.task.TaskInfo

  def createTaskWithId(
    difficulty: Int,
    id: String
  ): TaskWithId = TaskWithId(
    id = id,
    difficulty = difficulty
  )

  def createTaskInfo(
    id: String,
    difficulty: Int,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = UUID.fromString(id),
    difficulty = Difficulty(difficulty),
    status = status
  )

  def finishedTaskStatus: TaskStatus =
    TaskStatus.Finished

  def queuedTaskStatus: TaskStatus =
    TaskStatus.Queued

  def inProgressTaskStatus: TaskStatus =
    TaskStatus.InProgress

  implicit class TaskTransformers(task: TaskInfo) {
    def withDifficulty(difficulty: Int): TaskInfo =
      task.copy(difficulty = Difficulty(difficulty))
    def withStatus(status: TaskStatus): TaskInfo =
      task.copy(status = status)
  }

  def createTaskSlickRepository(database: Database): TaskSlickRepository =
    new TaskSlickRepository(database, new TaskStatusCodec)

  def saveTaskToRepository(repository: TaskSlickRepository)
      (task: TaskInfo): Future[_] =
    repository.save(task)

  def saveTasksToRepositoryInSequence(repository: TaskSlickRepository)
      (tasks: Seq[TaskInfo])(implicit ec: ExecutionContext): Future[Any] =
    tasks.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
      acc.flatMap(_ => saveTaskToRepository(repository)(task))
    }

  def findTaskInRepository[Q](repository: TaskRepository[Q])(id: UUID)
      (implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    repository.findById(id)

  def listTasksFromRepository(repository: TaskSlickRepository)
      (limit: Int, offset: Int)
      (implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = {
    val query = LimitOffsetQueryTestFacade.createQuery(limit, offset)
    repository.list(query)
  }

  /** Проверить, что задача существует в репозитории */
  def assertTaskExistsInRepository[Q](repository: TaskRepository[Q])
      (task: TaskInfo)(implicit ec: ExecutionContext): Future[Assertion] =
    for (taskOpt <- findTaskInRepository(repository)(task.id)) yield
      taskOpt shouldEqual Some(task)

  /** Сохранить задачу и проверить, что она сохранена */
  def saveTaskToRepositoryAndAssertSaved(repository: TaskSlickRepository)
    (task: TaskInfo)(implicit ec: ExecutionContext): Future[Assertion] =
    for {
      _ <- saveTaskToRepository(repository)(task)
      succeeded <- assertTaskExistsInRepository(repository)(task)
    } yield succeeded

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

object TaskTestFacade
  extends TaskTestFacade