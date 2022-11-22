package rr64.developer.domain

import rr64.developer.domain.dev.Developer

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Фасад сервиса разработки задач на основе разработчика и сервиса задач
 * @param developer Разработчик
 * @param taskService Сервис задач
 * @tparam Query Параметры запроса списка задач
 */
class DeveloperServiceFacade[Query](
  developer: Developer,
  taskService: Tasks[Query]
) extends DeveloperService[Query] {

  override def addTask(task: Task)(implicit ec: ExecutionContext): Future[DeveloperReply] =
    developer.addTask(task)

  override def developerState(implicit ec: ExecutionContext): Future[DeveloperState] =
    developer.state

  override def taskInfo(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    taskService.findById(id)

  override def tasks(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
    taskService.list(query)

}
