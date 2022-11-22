package rr64.developer.domain

import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID

/**
 * Информация о существующей задаче
 * @param id Идентификатор
 * @param difficulty Сложность
 * @param status Текущий статус
 * */
case class TaskInfo(id: UUID, difficulty: Difficulty, status: TaskStatus)

object TaskInfo {

  /**
   * Создать объект с информацией о задаче на основе задачи и статуса
   * @param taskWithId Задача
   * @param status Статус задачи
   * */
  def fromTaskAndStatus(
    taskWithId: TaskWithId,
    status: TaskStatus
  ): TaskInfo = TaskInfo(
    id = taskWithId.id,
    difficulty = taskWithId.difficulty,
    status = status
  )

  implicit class TaskInfoFromTaskWithId(taskWithId: TaskWithId) {
    /**
     * Добавить к задаче статус
     * @param status Статус задачи
     * */
    def withStatus(status: TaskStatus): TaskInfo =
      fromTaskAndStatus(taskWithId, status)
  }

}
