package rr64.developer.infrastructure.facade.task

import rr64.developer.domain.task.Task

/**
 * Тестовый заместитель для задач
 * */
case class TestTask(difficulty: Int) {

  /** Преобразовать в доменную задачу */
  private[facade] def toDomain: Task = Task(difficulty)

}
