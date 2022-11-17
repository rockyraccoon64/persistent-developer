package rr64.developer.infrastructure.task
import rr64.developer.domain.TaskInfo

import java.util.UUID
import scala.concurrent.Future

class TaskSlickRepository extends TaskRepository {

  override def save(taskInfo: TaskInfo): Future[_] = ???

  override def findById(id: UUID): Future[Option[TaskInfo]] = ???

  override def list: Future[Seq[TaskInfo]] = ???

}
