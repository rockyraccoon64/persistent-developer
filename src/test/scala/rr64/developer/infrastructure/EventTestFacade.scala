package rr64.developer.infrastructure

import rr64.developer.infrastructure.dev.behavior.Event
import rr64.developer.infrastructure.task.TaskWithId

trait EventTestFacade {

  def taskStartedEvent(taskWithId: TaskWithId): Event.TaskStarted =
    Event.TaskStarted(taskWithId)

  def taskQueuedEvent(taskWithId: TaskWithId): Event.TaskQueued =
    Event.TaskQueued(taskWithId)

}

object EventTestFacade
  extends EventTestFacade