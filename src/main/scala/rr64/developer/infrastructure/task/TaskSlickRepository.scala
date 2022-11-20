package rr64.developer.infrastructure.task
import rr64.developer.domain.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.task.TaskSlickRepository.TaskStatusAdapter
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class TaskSlickRepository(db: Database) extends TaskRepository {

  override def save(taskInfo: TaskInfo): Future[_] = db.run {
    val status = TaskStatusAdapter.toStringValue(taskInfo.status)
    sqlu"""INSERT INTO task(id, difficulty, status)
          VALUES (${taskInfo.id.toString}::uuid, ${taskInfo.difficulty.value}, $status)
          ON CONFLICT (id)
          DO UPDATE SET status = $status
        """
  }

  override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] = {
    db.run {
      sql"""SELECT difficulty, status FROM task
          WHERE id = ${id.toString}::uuid
        """.as[(Int, String)]
        .headOption
        .map(_.map { case (difficulty, statusStr) =>
          val status = TaskStatusAdapter.fromString(statusStr)
          TaskInfo(id, Difficulty(difficulty), status)
        })
    }
  }

  override def list(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = {
    db.run {
      sql"""SELECT id, difficulty, status FROM task"""
        .as[(String, Int, String)]
        .map(_.map { case (idStr, difficulty, statusStr) =>
          val id = UUID.fromString(idStr)
          val status = TaskStatusAdapter.fromString(statusStr)
          TaskInfo(id, Difficulty(difficulty), status)
        })
    }
  }

}

object TaskSlickRepository {

  private object TaskStatusAdapter {

    def toStringValue(status: TaskStatus): String = status match {
      case TaskStatus.InProgress => "InProgress"
      case TaskStatus.Queued => "Queued"
      case TaskStatus.Finished => "Finished"
    }

    def fromString(value: String): TaskStatus = value match {
      case "InProgress" => TaskStatus.InProgress
      case "Queued" => TaskStatus.Queued
      case "Finished" => TaskStatus.Finished
    }

  }

}