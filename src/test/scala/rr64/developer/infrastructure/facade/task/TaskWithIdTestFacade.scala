package rr64.developer.infrastructure.facade.task

import rr64.developer.infrastructure.task.TaskWithId

/**
 * Фасад для тестов с использованием TaskWithId
 */
trait TaskWithIdTestFacade {

  /** Создать задачу с идентификатором */
  def createTaskWithId(
    difficulty: Int,
    id: String
  ): TaskWithId = TaskWithId(
    id = id,
    difficulty = difficulty
  )

}

object TaskWithIdTestFacade
  extends TaskWithIdTestFacade