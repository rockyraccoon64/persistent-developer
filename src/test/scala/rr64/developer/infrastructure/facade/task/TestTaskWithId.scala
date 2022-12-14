package rr64.developer.infrastructure.facade.task

import rr64.developer.infrastructure.task.TaskWithId

/**
 * Тестовый заместитель для задач с идентификатором
 * */
case class TestTaskWithId(difficulty: Int, id: String) {

  /** Извлечь задачу без идентификатора */
  def toTask: TestTask = TestTask(difficulty)

  /** Преобразовать в доменную задачу с идентификатором */
  private[facade] def toDomain: TaskWithId =
    TaskTestFacade.createTaskWithId(difficulty, id)

}
