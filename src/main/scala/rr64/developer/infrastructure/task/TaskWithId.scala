package rr64.developer.infrastructure.task

import rr64.developer.domain.task.{Difficulty, Task, TaskInfo, TaskStatus}

import java.util.UUID

/**
 * Задача с присвоенным ей идентификатором
 * @param task Задача
 * @param id Идентификатор задачи
 * */
case class TaskWithId(task: Task, id: UUID) {

  /** Сложность задачи */
  def difficulty: Difficulty = task.difficulty

  /**
   * Добавить к задаче статус
   * @param status Статус задачи
   * */
  def withStatus(status: TaskStatus): TaskInfo =
    TaskInfo(
      id = id,
      difficulty = difficulty,
      status = status
    )

}

object TaskWithId {

  /**
   * Присвоить задаче идентификатор
   * @param task Задача
   * */
  def fromTask(task: Task): TaskWithId =
    TaskWithId(task, UUID.randomUUID())

  /**
   * Задача с присвоенным ей идентификатором
   * @param difficulty Сложность задачи
   * @param id Идентификатор задачи
   * */
  def apply(difficulty: Int, id: UUID): TaskWithId =
    TaskWithId(Task(difficulty), id)

  /**
   * Задача с присвоенным ей идентификатором
   * @param difficulty Сложность задачи
   * @param id Идентификатор задачи (UUID)
   * */
  def apply(difficulty: Int, id: String): TaskWithId =
    TaskWithId(Task(difficulty), UUID.fromString(id))

}