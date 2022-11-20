package rr64.developer.domain

case class Difficulty private(value: Int)

object Difficulty {

  def apply(value: Int): Difficulty = {
    if (value > 0 && value <= 100)
      new Difficulty(value)
    else
      throw new TaskDifficultyException
  }

  class TaskDifficultyException
    extends RuntimeException("Tasks should have difficulty [1-100]")

}
