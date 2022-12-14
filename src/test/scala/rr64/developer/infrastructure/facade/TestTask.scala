package rr64.developer.infrastructure.facade

import rr64.developer.domain.task.Task

case class TestTask(difficulty: Int) {
  private[facade] def toDomain: Task = Task(difficulty)
}
