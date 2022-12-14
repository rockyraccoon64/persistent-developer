package rr64.developer.infrastructure.facade.task

import rr64.developer.infrastructure.task.TaskWithId

case class TestTaskWithId(difficulty: Int, id: String) {
  def toTask: TestTask = TestTask(difficulty)

  private[facade] def toDomain: TaskWithId =
    TaskTestFacade.createTaskWithId(difficulty, id)
}
