package rr64.developer.infrastructure.task

import rr64.developer.domain.{Difficulty, TaskInfo}
import rr64.developer.infrastructure.task.TaskSlickRepository.statusCodec
import rr64.developer.infrastructure.task.query.LimitOffsetQuery
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Репозиторий задач на основе PostgreSQL + Slick
 * @param db База данных PostgreSQL
 * */
class TaskSlickRepository(db: Database) extends TaskRepository[LimitOffsetQuery] {

  override def save(taskInfo: TaskInfo): Future[_] = db.run {
    val status = statusCodec.encode(taskInfo.status)
    sqlu"""INSERT INTO task(uuid, difficulty, status)
          VALUES (${taskInfo.id.toString}::uuid, ${taskInfo.difficulty.value}, $status)
          ON CONFLICT (uuid)
          DO UPDATE SET status = $status
        """
  }

  override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] = {
    db.run {
      sql"""SELECT difficulty, status FROM task
          WHERE uuid = ${id.toString}::uuid
        """.as[(Int, String)]
        .headOption
        .map(_.map { case (difficulty, statusStr) =>
          val status = statusCodec.decode(statusStr)
          TaskInfo(id, Difficulty(difficulty), status)
        })
    }
  }

  override def list(query: LimitOffsetQuery)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = {
    db.run {
      sql"""SELECT uuid, difficulty, status
           FROM task
           ORDER BY serial_id
           OFFSET ${query.offset}
           LIMIT ${query.limit}"""
        .as[(String, Int, String)]
        .map(_.map { case (idStr, difficulty, statusStr) =>
          val id = UUID.fromString(idStr)
          val status = statusCodec.decode(statusStr)
          TaskInfo(id, Difficulty(difficulty), status)
        })
    }
  }

}

object TaskSlickRepository {

  /** Кодек доменных статусов задач и статусов из БД */
  private val statusCodec = new TaskStatusCodec

}