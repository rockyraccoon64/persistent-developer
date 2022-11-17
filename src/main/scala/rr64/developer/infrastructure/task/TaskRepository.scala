package rr64.developer.infrastructure.task

import rr64.developer.domain.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait TaskRepository {
  def save(taskInfo: TaskInfo): Future[_]
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def list: Future[Seq[TaskInfo]]
}
