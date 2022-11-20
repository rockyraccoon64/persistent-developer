package rr64.developer.domain

case class Difficulty private(value: Int)

object Difficulty {

  def apply(value: Int): Difficulty = {
    if (value > 0 && value <= 100)
      new Difficulty(value)
    else
      throw new DifficultyException
  }

  class DifficultyException
    extends RuntimeException("Difficulty should be in the range [1-100]")

}
