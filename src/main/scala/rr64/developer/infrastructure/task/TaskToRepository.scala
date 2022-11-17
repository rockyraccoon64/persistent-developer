package rr64.developer.infrastructure.task

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.DeveloperBehavior.Event

import scala.concurrent.{ExecutionContext, Future}

class TaskToRepository(repository: TaskRepository)
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {
  override def process(envelope: EventEnvelope[Event]): Future[Done] =
    envelope.event match {
      case Event.TaskStarted(taskWithId) =>
        val taskInfo = TaskInfo(
          id = taskWithId.id,
          difficulty = taskWithId.task.difficulty,
          status = TaskStatus.InProgress
        )
        repository.save(taskInfo).map(_ => Done)
      case Event.TaskQueued(taskWithId) =>
        val taskInfo = TaskInfo(
          id = taskWithId.id,
          difficulty = taskWithId.task.difficulty,
          status = TaskStatus.Queued
        )
        repository.save(taskInfo).map(_ => Done)
      case Event.TaskFinished => ???
      case Event.Rested => ???
    }
}
