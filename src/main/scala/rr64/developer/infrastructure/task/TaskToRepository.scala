package rr64.developer.infrastructure.task

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.DeveloperBehavior.{Event, TaskWithId}
import rr64.developer.infrastructure.task.TaskToRepository.TaskOps

import scala.concurrent.{ExecutionContext, Future}

class TaskToRepository(repository: TaskRepository)
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {

  override def process(envelope: EventEnvelope[Event]): Future[Done] =
    envelope.event match {
      case Event.TaskStarted(taskWithId) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
        save(taskInfo)

      case Event.TaskQueued(taskWithId) =>
        val taskInfo = taskWithId.withStatus(TaskStatus.Queued)
        save(taskInfo)

      case Event.TaskFinished => ???
      case Event.Rested => ???
    }

  private def save(taskInfo: TaskInfo): Future[Done] =
    repository.save(taskInfo).map(_ => Done)

}

object TaskToRepository {
  implicit class TaskOps(taskWithId: TaskWithId) {
    def withStatus(status: TaskStatus): TaskInfo = TaskInfo(
      id = taskWithId.id,
      difficulty = taskWithId.task.difficulty,
      status = status
    )
  }
}
