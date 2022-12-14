package rr64.developer.domain.task

/**
 * Задача
 *
 * @param difficulty Сложность задачи
 * */
case class Task(difficulty: Difficulty)

object Task {
  def apply(difficulty: Int): Task = Task(Difficulty(difficulty))
}