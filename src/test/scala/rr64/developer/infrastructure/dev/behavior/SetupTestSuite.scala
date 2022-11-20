package rr64.developer.infrastructure.dev.behavior

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.dev.behavior.Setup.FactorException

class SetupTestSuite extends AnyWordSpec with Matchers {

  private def setupFromWorkFactor(workFactor: Int) = Setup(
    workFactor = workFactor,
    restFactor = ???,
    timer = ???
  )

  /** Рабочий множитель */
  "The work factor" should {

    /** Не должен быть меньше единицы */
    "not be allowed to be less than 1" in {
      assertThrows[FactorException] {
        setupFromWorkFactor(0)
      }
      assertThrows[FactorException] {
        setupFromWorkFactor(-1)
      }
      assertThrows[FactorException] {
        setupFromWorkFactor(-15)
      }
    }
  }

}
