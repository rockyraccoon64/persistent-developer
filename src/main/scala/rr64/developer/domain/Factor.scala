package rr64.developer.domain

case class Factor private(value: Int)

object Factor {

  def apply(value: Int): Factor =
    if (value > 0 && value <= 1000)
      new Factor(value)
    else
      throw new FactorException

  class FactorException extends RuntimeException

}