package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.TimerScheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.dev.behavior.Setup.FactorException

class SetupTestSuite extends AnyWordSpec with Matchers with MockFactory {

  /** Рабочий множитель */
  "The work factor" should {

    def setupFromWorkFactor(workFactor: Int) = Setup(
      workFactor = workFactor,
      restFactor = 10,
      timer = mock[TimerScheduler[Command]]
    )

    def assertFactorException(workFactor: Int): Assertion =
      assertThrows[FactorException] {
        setupFromWorkFactor(workFactor)
      }

    /** Не должен быть меньше единицы */
    "not be allowed to be less than 1" in {
      assertFactorException(0)
      assertFactorException(-1)
      assertFactorException(-15)
    }

    /** Не должен быть больше 1000 */
    "not be allowed to be greater than 1000" in {
      assertFactorException(1001)
      assertFactorException(987654)
    }

  }

}
