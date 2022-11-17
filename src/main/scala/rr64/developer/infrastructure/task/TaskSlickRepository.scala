package rr64.developer.infrastructure.task
import rr64.developer.domain.{TaskInfo, TaskStatus}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future

class TaskSlickRepository(db: Database) extends TaskRepository {

  override def save(taskInfo: TaskInfo): Future[_] = db.run {
    val status = taskInfo.status match {
      case TaskStatus.InProgress => "InProgress"
      case TaskStatus.Queued => "Queued"
      case TaskStatus.Finished => "Finished"
    }
    sqlu"""INSERT INTO task(id, difficulty, status)
          VALUES (${taskInfo.id.toString}::uuid, ${taskInfo.difficulty}, $status)
          ON CONFLICT (id)
          DO UPDATE SET status = $status
        """
  }

  override def findById(id: UUID): Future[Option[TaskInfo]] =
    Future.successful(
      Some(
        TaskInfo(
          id = UUID.fromString("30dbff1f-88dc-4972-aa70-a057bf5f1c88"),
          difficulty = 5,
          status = TaskStatus.Queued
        )
      )
    )

  override def list: Future[Seq[TaskInfo]] = ???

}
