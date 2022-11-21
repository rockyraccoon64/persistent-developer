package rr64.developer.infrastructure.task

import rr64.developer.domain.{TaskInfo, Tasks}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class TasksFromRepository[Query](repository: TaskRepository[Query]) extends Tasks[Query] {
  override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    repository.findById(id)
  override def list(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
    repository.list(query)
}
