package rr64.developer.infrastructure.task

import rr64.developer.domain.TaskInfo

import java.util.UUID
import scala.concurrent.Future

trait TaskRepository {
  def save(taskInfo: TaskInfo): Future[_]
  def findById(id: UUID): Future[Option[TaskInfo]]
}
