package rr64.developer.domain

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait Tasks {
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def list(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
