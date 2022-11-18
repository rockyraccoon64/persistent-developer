package rr64.developer.domain

import java.util.UUID
import scala.concurrent.Future

trait DeveloperService {
  def addTask(task: Task): Future[Nothing]
  def developerState: Future[DeveloperState]
  def taskInfo(id: UUID): Future[Option[TaskInfo]]
  def tasks: Future[Seq[TaskInfo]]
}
