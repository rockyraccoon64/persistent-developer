package rr64.developer.domain.task

/**
 * Сложность задачи
 * @param value Значение в рамках 1-100
 */
case class Difficulty private(value: Int)

object Difficulty {

  def apply(value: Int): Difficulty =
    if (value > 0 && value <= 100)
      new Difficulty(value)
    else
      throw new DifficultyException

  /** Исключение, связанное с некорректной сложностью задачи */
  class DifficultyException
    extends RuntimeException("Difficulty should be in the range [1-100]")

}
