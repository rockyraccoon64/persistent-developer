package rr64.developer.domain

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait Tasks[Query] {
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]
  def list(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
