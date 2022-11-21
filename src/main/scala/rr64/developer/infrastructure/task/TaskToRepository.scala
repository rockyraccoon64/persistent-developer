package rr64.developer.infrastructure.task

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.dev.behavior.Event
import rr64.developer.infrastructure.task.TaskToRepository.TaskInfoFactory

import scala.concurrent.{ExecutionContext, Future}

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

  private def save(taskInfo: TaskInfo): Future[Done] =
    repository.save(taskInfo).map(_ => Done)

}

object TaskToRepository {
  implicit class TaskInfoFactory(taskWithId: TaskWithId) {
    def withStatus(status: TaskStatus): TaskInfo = TaskInfo(
      id = taskWithId.id,
      difficulty = taskWithId.difficulty,
      status = status
    )
  }
}
