package rr64.developer.domain

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait DeveloperService[Query] {
  def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply]
  def developerState(implicit ec: ExecutionContext): Future[DeveloperState]
  def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def tasks(q: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
