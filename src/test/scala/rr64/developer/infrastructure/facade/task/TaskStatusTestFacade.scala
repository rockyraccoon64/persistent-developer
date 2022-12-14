package rr64.developer.infrastructure.facade.task

import rr64.developer.domain.task.TaskStatus

/**
 * Фасад для тестов с использованием статуса задач
 */
trait TaskStatusTestFacade {

  /** Задача в очереди */
  def queuedTaskStatus: TaskStatus =
    TaskStatus.Queued

  /** Задача в работе */
  def inProgressTaskStatus: TaskStatus =
    TaskStatus.InProgress

  /** Задача завершена */
  def finishedTaskStatus: TaskStatus =
    TaskStatus.Finished

}

object TaskStatusTestFacade
  extends TaskStatusTestFacade