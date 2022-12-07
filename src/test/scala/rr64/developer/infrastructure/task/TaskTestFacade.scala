package rr64.developer.infrastructure.task

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.task.query.LimitOffsetQuery
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait TaskTestFacade {

  def createTaskInfo(
    id: UUID,
    difficulty: Difficulty,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = id,
    difficulty = difficulty,
    status = status
  )

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

  def findTaskInRepository(repository: TaskSlickRepository)(id: UUID)
      (implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    repository.findById(id)

  def listTasksFromRepository(repository: TaskSlickRepository)
      (query: LimitOffsetQuery)
      (implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
    repository.list(query)

}

object TaskTestFacade
  extends TaskTestFacade