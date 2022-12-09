package rr64.developer.infrastructure

import rr64.developer.infrastructure.dev.behavior.Event
import rr64.developer.infrastructure.task.TaskWithId

trait EventTestFacade {

  type Event = rr64.developer.infrastructure.dev.behavior.Event

  def taskStartedEvent(taskWithId: TaskWithId): Event.TaskStarted =
    Event.TaskStarted(taskWithId)

  def taskQueuedEvent(taskWithId: TaskWithId): Event.TaskQueued =
    Event.TaskQueued(taskWithId)

  def taskFinishedEvent(taskWithId: TaskWithId): Event.TaskFinished =
    Event.TaskFinished(taskWithId)

  def restedEvent(nextTask: Option[TaskWithId]): Event.Rested =
    Event.Rested(nextTask)

}

object EventTestFacade
  extends EventTestFacade