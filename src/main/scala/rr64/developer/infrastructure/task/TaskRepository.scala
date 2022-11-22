package rr64.developer.infrastructure.task

import rr64.developer.domain.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Репозиторий задач
 * @tparam Query Параметры запроса списка задач
 * */
trait TaskRepository[Query] {

  /**
   * Сохранить задачу
   * @param taskInfo Задача
   * */
  def save(taskInfo: TaskInfo): Future[_]

  /**
   * Найти задачу по идентификатору
   * @param id Идентификатор задачи
   * */
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]

  /**
   * Запрос списка задач
   * @param query Параметры запроса
   * */
  def list(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]
}
