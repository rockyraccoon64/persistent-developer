package rr64.developer.infrastructure.dev.behavior

import rr64.developer.infrastructure.task.TaskWithId

/**
 * Событие актора разработчика
 * */
sealed trait Event

object Event {
  /** Задача принята в разработку */
  case class TaskStarted(taskWithId: TaskWithId) extends Event
  /** Задача поставлена в очередь */
  case class TaskQueued(taskWithId: TaskWithId) extends Event
  /** Задача завершена */
  case class TaskFinished(taskWithId: TaskWithId) extends Event
  /** Отдых завершён */
  case class Rested(nextTask: Option[TaskWithId]) extends Event
}
