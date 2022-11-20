package rr64.developer.domain

case class Task private(difficulty: Int)

object Task {
  def apply(difficulty: Int): Task = {
    if (difficulty > 0 && difficulty <= 100)
      new Task(difficulty)
    else
      throw new IllegalArgumentException
  }
}