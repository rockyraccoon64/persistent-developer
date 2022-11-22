package rr64.developer.infrastructure.task

import rr64.developer.domain.Tasks
import rr64.developer.domain.task.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Сервис поиска задач на основе репозитория
 * @tparam Query Параметры запроса списка задач
 * @param repository Репозиторий задач
 * */
class TasksFromRepository[Query](repository: TaskRepository[Query]) extends Tasks[Query] {
  override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    repository.findById(id)
  override def list(query: Query)(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
    repository.list(query)
}
