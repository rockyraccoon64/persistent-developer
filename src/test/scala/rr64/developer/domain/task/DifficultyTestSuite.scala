package rr64.developer.domain.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.task.Difficulty.DifficultyException

/**
 * Тесты сложности задач
 */
class DifficultyTestSuite extends AnyWordSpec with Matchers {

  private def assertException(difficulty: Int): Assertion =
    assertThrows[DifficultyException](Difficulty(difficulty))

  private def assertNoException(difficulty: Int): Assertion =
    noException should be thrownBy Difficulty(difficulty)

  /** Сложность задач должна быть */
  "Difficulty" should {

    /** Не меньше единицы */
    "not be allowed to be less than 1" in {
      assertException(0)
      assertException(-1)
      assertException(-55)
    }

    /** Не больше 100 */
    "not be allowed to be greater than 100" in {
      assertException(101)
      assertException(12345)
    }

    /** В пределах 1-100 */
    "be allowed to be in the range [1-100]" in {
      assertNoException(1)
      assertNoException(5)
      assertNoException(55)
      assertNoException(100)
    }
  }

}
