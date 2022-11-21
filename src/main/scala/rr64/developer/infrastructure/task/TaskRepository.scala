package rr64.developer.infrastructure.task

import rr64.developer.domain.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait TaskRepository[Query] {
  def save(taskInfo: TaskInfo): Future[_]
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def list(q: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
