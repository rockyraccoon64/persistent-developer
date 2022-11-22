package rr64.developer.domain

import rr64.developer.domain.task.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Сервис поиска задач
 * @tparam Query Параметры запроса списка задач
 */
trait Tasks[Query] {

  /**
   * Найти задачу по идентификатору
   * @param id Идентификатор задачи
   * */
  def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]

  /**
   * Запросить список задач
   * @param query Параметры запроса
   * */
  def list(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]

}
