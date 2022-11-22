package rr64.developer.domain

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.timing.Factor
import rr64.developer.domain.timing.Factor.FactorException

class FactorTestSuite extends AnyWordSpec with Matchers {

  /** Множители */
  "Factors" should {

    def assertFactorException(factor: Int): Assertion =
      assertThrows[FactorException] {
        Factor(factor)
      }

    def assertNoException(factor: Int): Assertion =
      noException should be thrownBy Factor(factor)

    /** Не должны быть меньше единицы */
    "not be allowed to be less than 1" in {
      assertFactorException(0)
      assertFactorException(-1)
      assertFactorException(-15)
    }

    /** Не должны быть больше 1000 */
    "not be allowed to be greater than 1000" in {
      assertFactorException(1001)
      assertFactorException(987654)
    }

    /** Должны быть в пределах 1-1000 */
    "be allowed to be between 1 and 1000" in {
      assertNoException(1)
      assertNoException(445)
      assertNoException(1000)
    }

  }

}
