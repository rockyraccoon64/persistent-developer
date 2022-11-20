package rr64.developer.domain

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * Тесты создания задач
 */
class TaskTestSuite extends AnyWordSpec with Matchers {

  "Tasks" should {

    def assertException(difficulty: Int): Assertion =
      assertThrows[IllegalArgumentException](Task(difficulty))

    def assertNoException(difficulty: Int): Assertion =
      noException should be thrownBy Task(difficulty)

    /** Сложность задач должна быть больше нуля */
    "only be allowed to have positive difficulty" in {
      assertException(-55)
      assertException(-1)
      assertException(0)
      assertNoException(1)
      assertNoException(5)
    }

    /** Сложность задач должна быть меньше или равна 100 */
    "only be allowed to have difficulty less than or equal to 100" in {
      assertException(12345)
      assertException(101)
      assertNoException(100)
      assertNoException(55)
    }
  }

}
