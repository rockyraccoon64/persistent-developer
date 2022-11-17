package rr64.developer.infrastructure.task

import rr64.developer.domain.{TaskInfo, Tasks}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class TasksFromRepository(repository: TaskRepository) extends Tasks {
  override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] = ???
  override def list(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = ???
}
