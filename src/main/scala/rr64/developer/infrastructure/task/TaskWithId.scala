package rr64.developer.infrastructure.task

import rr64.developer.domain.{Difficulty, Task}

import java.util.UUID

case class TaskWithId(task: Task, id: UUID) {
  def difficulty: Difficulty = task.difficulty
}

object TaskWithId {

  def fromTask(task: Task): TaskWithId =
    TaskWithId(task, UUID.randomUUID())

  def apply(difficulty: Int, id: UUID): TaskWithId =
    TaskWithId(Task(difficulty), id)

}