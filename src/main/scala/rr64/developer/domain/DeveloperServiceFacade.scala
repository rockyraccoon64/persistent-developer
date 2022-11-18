package rr64.developer.domain
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class DeveloperServiceFacade(developer: Developer, tasks: Tasks) extends DeveloperService {

  override def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply] =
    developer.addTask(task)

  override def developerState(implicit ec: ExecutionContext): Future[DeveloperState] =
    developer.state

  override def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    tasks.findById(id)

  override def tasks(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
    tasks.list

}
