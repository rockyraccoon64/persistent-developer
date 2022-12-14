package rr64.developer.infrastructure.facade.task

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}

import java.util.UUID

/**
 * Фасад для тестов с использованием TaskInfo
 * */
trait TaskInfoTestFacade {

  /** Информация о существующей задаче */
  type TaskInfo = rr64.developer.domain.task.TaskInfo

  /** Создать объект с информацией о существующей задаче */
  def createTaskInfo(
    id: String,
    difficulty: Int,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = UUID.fromString(id),
    difficulty = Difficulty(difficulty),
    status = status
  )

  implicit class TaskInfoTransformers(task: TaskInfo) {

    /** Задача с другой сложностью */
    def withDifficulty(difficulty: Int): TaskInfo =
      task.copy(difficulty = Difficulty(difficulty))

    /** Задача с другим статусом */
    def withStatus(status: TaskStatus): TaskInfo =
      task.copy(status = status)

  }

}

object TaskInfoTestFacade
  extends TaskInfoTestFacade