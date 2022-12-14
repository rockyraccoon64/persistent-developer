package rr64.developer.infrastructure.facade.event

import rr64.developer.infrastructure.dev.behavior.Event
import rr64.developer.infrastructure.task.TaskWithId

/**
 * Фасад для тестов с использованием событий актора разработчика
 * */
trait DeveloperEventTestFacade {

  /** Событие */
  type Event = rr64.developer.infrastructure.dev.behavior.Event

  /** Задача начата */
  def taskStartedEvent(taskWithId: TaskWithId): Event.TaskStarted =
    Event.TaskStarted(taskWithId)

  /** Задача поставлена в очередь */
  def taskQueuedEvent(taskWithId: TaskWithId): Event.TaskQueued =
    Event.TaskQueued(taskWithId)

  /** Задача завершена */
  def taskFinishedEvent(taskWithId: TaskWithId): Event.TaskFinished =
    Event.TaskFinished(taskWithId)

  /** Отдых завершён */
  def restedEvent(nextTask: Option[TaskWithId]): Event.Rested =
    Event.Rested(nextTask)

}

object DeveloperEventTestFacade
  extends DeveloperEventTestFacade