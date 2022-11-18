package rr64.developer.infrastructure.dev.behavior

import rr64.developer.infrastructure.task.TaskWithId

sealed trait Event
object Event {
  case class TaskStarted(taskWithId: TaskWithId) extends Event
  case class TaskQueued(taskWithId: TaskWithId) extends Event
  case class TaskFinished(taskWithId: TaskWithId) extends Event
  case object Rested extends Event
}
