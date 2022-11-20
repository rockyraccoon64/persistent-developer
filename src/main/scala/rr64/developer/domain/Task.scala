package rr64.developer.domain

case class Task(difficulty: Difficulty)

object Task {
  def apply(difficulty: Int): Task = Task(Difficulty(difficulty))
}