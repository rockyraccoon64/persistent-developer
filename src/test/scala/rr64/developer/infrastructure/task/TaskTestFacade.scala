package rr64.developer.infrastructure.task

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

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

}

object TaskTestFacade
  extends TaskTestFacade