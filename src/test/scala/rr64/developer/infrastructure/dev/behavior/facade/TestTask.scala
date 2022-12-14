package rr64.developer.infrastructure.dev.behavior.facade

import rr64.developer.domain.task.Task
import rr64.developer.infrastructure.task.TaskWithId

case class TestTask(difficulty: Int) {
  private[facade] def toDomain: Task = Task(difficulty)
}

object TestTask {
  def fromTaskWithId(taskWithId: TaskWithId): TestTask = // TODO Убрать
    TestTask(taskWithId.difficulty.value)
}