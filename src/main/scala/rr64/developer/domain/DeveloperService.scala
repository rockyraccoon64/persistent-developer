package rr64.developer.domain

import rr64.developer.domain.dev.DeveloperReply

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Сервис разработки задач
 * @tparam Query Параметры запроса списка задач
 * */
trait DeveloperService[Query] {

  /**
   * Поручить разработчику задачу
   * @param task Новая задача
   * */
  def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply]

  /** Текущее состояние разработчика */
  def developerState(implicit ec: ExecutionContext): Future[DeveloperState]

  /**
   * Информация о задаче
   * @param id Идентификатор задачи
   * */
  def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]]

  /**
   * Запросить список задач
   * @param query Параметры запроса
   * */
  def tasks(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]]

}
