package rr64.developer.domain

case class Task(_difficulty: Difficulty) {
  def difficulty: Int = _difficulty.value
}

object Task {
  def apply(difficulty: Int): Task = Task(Difficulty(difficulty))
}