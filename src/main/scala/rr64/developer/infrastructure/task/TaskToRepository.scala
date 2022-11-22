package rr64.developer.infrastructure.task

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.TaskInfo.TaskInfoFactory
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.dev.behavior.Event

import scala.concurrent.{ExecutionContext, Future}

/**
 * Обработчик проекции информации о задачах
 * @param repository Репозиторий задач
 * */
class TaskToRepository(repository: TaskRepository[_])
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {

  override def process(envelope: EventEnvelope[Event]): Future[Done] =
    envelope.event match {
      case Event.TaskStarted(taskWithId) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
        save(taskInfo)

      case Event.TaskQueued(taskWithId) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.Queued)
        save(taskInfo)

      case Event.TaskFinished(taskWithId) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.Finished)
        save(taskInfo)

      case Event.Rested(Some(taskWithId)) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
        save(taskInfo)

      case Event.Rested(_) =>
        Future.successful(Done)
    }

  /**
   * Сохранить задачу в репозитории
   * @param taskInfo Задача
   * */
  private def save(taskInfo: TaskInfo): Future[Done] =
    repository.save(taskInfo).map(_ => Done)

}

