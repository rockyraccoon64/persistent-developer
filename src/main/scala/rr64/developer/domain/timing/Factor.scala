package rr64.developer.domain.timing

/**
 * Множитель для расчёта времени
 * @param value Значение в рамках 1-1000
 * */
case class Factor private(value: Int)

object Factor {

  def apply(value: Int): Factor =
    if (value > 0 && value <= 1000)
      new Factor(value)
    else
      throw new FactorException

  /** Исключение, связанное с некорректным множителем */
  class FactorException extends RuntimeException

}