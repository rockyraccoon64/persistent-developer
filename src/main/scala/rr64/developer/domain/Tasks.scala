package rr64.developer.domain

import java.util.UUID
import scala.concurrent.Future

trait Tasks {
  def findById(id: UUID): Future[Option[TaskInfo]]
  def list: Future[Seq[TaskInfo]]
}
