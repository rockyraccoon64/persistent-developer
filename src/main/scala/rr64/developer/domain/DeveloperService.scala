package rr64.developer.domain

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait DeveloperService {
  def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply]
  def developerState(implicit ec: ExecutionContext): Future[DeveloperState]
  def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def tasks(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
