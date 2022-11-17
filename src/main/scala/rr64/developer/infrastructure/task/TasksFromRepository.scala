package rr64.developer.infrastructure.task

import rr64.developer.domain.{TaskInfo, Tasks}

import java.util.UUID
import scala.concurrent.Future

class TasksFromRepository(repository: TaskRepository) extends Tasks {
  override def findById(id: UUID): Future[Option[TaskInfo]] = repository.findById(id)
  override def list: Future[Seq[TaskInfo]] = repository.list
}
