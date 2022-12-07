package rr64.developer.infrastructure.task

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}

import java.util.UUID

trait TaskTestFacade {

  def createTaskInfo(
    id: UUID,
    difficulty: Difficulty,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = id,
    difficulty = difficulty,
    status = status
  )

}

object TaskTestFacade
  extends TaskTestFacade