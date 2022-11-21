package rr64.developer.infrastructure.task.query

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * Тесты фабрики параметров запроса
 */
class LimitOffsetQueryFactoryTestSuite
  extends AnyWordSpec
    with Matchers {

  trait FactoryTest {

    def assertException(defaultLimit: Int, maxLimit: Int): Assertion = {
      assertThrows[IllegalArgumentException] {
        new LimitOffsetQueryFactory(defaultLimit = defaultLimit, maxLimit = maxLimit)
      }
    }

    def assertNoException(defaultLimit: Int, maxLimit: Int): Assertion =
      noException should be thrownBy {
        new LimitOffsetQueryFactory(defaultLimit = defaultLimit, maxLimit = maxLimit)
      }

  }

  /** Фабрика параметров запроса */
  "The factory" should {

    /** Должна иметь limit по умолчанию больше, чем 0 */
    "have a default limit greater than zero" in new FactoryTest {
      assertException(defaultLimit = -1, maxLimit = 30)
      assertException(defaultLimit = 0, maxLimit = 30)
      assertNoException(defaultLimit = 1, maxLimit = 30)
    }

    /** Должна иметь максимальный limit больше, чем 0 */
    "have a max limit greater than zero" in new FactoryTest {
      assertException(defaultLimit = 1, maxLimit = -1)
      assertException(defaultLimit = 1, maxLimit = 0)
      assertNoException(defaultLimit = 1, maxLimit = 1)
      assertNoException(defaultLimit = 1, maxLimit = 10)
    }

    /** Должна limit по умолчанию меньше или равный, чем максимальный */
    "have a default limit less than or equal to the max limit" in new FactoryTest {
      assertNoException(defaultLimit = 5, maxLimit = 10)
      assertNoException(defaultLimit = 10, maxLimit = 10)
      assertException(defaultLimit = 11, maxLimit = 10)
    }

  }

}
