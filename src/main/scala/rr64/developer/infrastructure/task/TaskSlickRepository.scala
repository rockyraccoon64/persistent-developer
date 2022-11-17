package rr64.developer.infrastructure.task
import rr64.developer.domain.{TaskInfo, TaskStatus}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

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

  override def findById(id: UUID): Future[Option[TaskInfo]] = {
    implicit val ec: ExecutionContext = global // TODO
    db.run {
      sql"""SELECT difficulty, status FROM task
          WHERE id = ${id.toString}::uuid
        """.as[(Int, String)]
        .headOption
        .map(_.map { case (difficulty, statusString) =>
          val status = statusString match {
            case "InProgress" => TaskStatus.InProgress
            case "Queued" => TaskStatus.Queued
            case "Finished" => TaskStatus.Finished
          }
          TaskInfo(id, difficulty, status)
        })
    }
  }

  override def list: Future[Seq[TaskInfo]] = ???

}
