package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.task.LimitOffsetQuery.LimitOffsetException

class LimitOffsetQueryTestSuite
  extends AnyWordSpec
    with Matchers {

  private val factory = new QueryFactory(defaultLimit = 10, maxLimit = 30)

  trait FactoryTest {

    def assertIllegal(defaultLimit: Int, maxLimit: Int): Assertion = {
      assertThrows[IllegalArgumentException] {
        new QueryFactory(defaultLimit = defaultLimit, maxLimit = maxLimit)
      }
    }

    def assertLegal(defaultLimit: Int, maxLimit: Int): Assertion =
      noException should be thrownBy {
        new QueryFactory(defaultLimit = defaultLimit, maxLimit = maxLimit)
      }

  }

  trait LimitOffsetTest {

    def assertException(limit: Int = 10, offset: Int = 0): Assertion =
      assertThrows[LimitOffsetException] {
        factory.create(limit = limit, offset = offset)
      }

    def assertNoException(limit: Int = 10, offset: Int = 0): Assertion =
      noException should be thrownBy factory.create(limit = limit, offset = offset)

  }

  /** Фабрика параметров запроса */
  "The factory" should {

    /** Должна иметь limit по умолчанию больше, чем 0 */
    "have a default limit greater than zero" in new FactoryTest {
      assertIllegal(defaultLimit = -1, maxLimit = 30)
      assertIllegal(defaultLimit = 0, maxLimit = 30)
      assertLegal(defaultLimit = 1, maxLimit = 30)
    }

    /** Должна иметь максимальный limit больше, чем 0 */
    "have a max limit greater than zero" in new FactoryTest {
      assertIllegal(defaultLimit = 1, maxLimit = -1)
      assertIllegal(defaultLimit = 1, maxLimit = 0)
      assertLegal(defaultLimit = 1, maxLimit = 1)
      assertLegal(defaultLimit = 1, maxLimit = 10)
    }

    /** Должна limit по умолчанию меньше или равный, чем максимальный */
    "have a default limit less than or equal to the max limit" in new FactoryTest {
      assertLegal(defaultLimit = 5, maxLimit = 10)
      assertLegal(defaultLimit = 10, maxLimit = 10)
      assertIllegal(defaultLimit = 11, maxLimit = 10)
    }

  }

  /** Количество запрашиваемых элементов */
  "The limit" should {

    /** Не должно быть меньше одного */
    "not be allowed to be less than 1" in new LimitOffsetTest {
      assertException(limit = 0)
      assertException(limit = -1)
    }

    /** Не должно быть больше максимального */
    "not be allowed to be greater than the max limit" in new LimitOffsetTest {
      assertException(limit = 31)
    }

    /** Может быть максимальным */
    "be allowed to be equal to the max limit" in new LimitOffsetTest {
      assertNoException(limit = 30)
    }

    /** Может быть больше нуля */
    "be allowed to be greater than zero" in new LimitOffsetTest {
      assertNoException(limit = 1)
      assertNoException(limit = 25)
    }

  }

  /** Номер первого запрашиваемого элемента */
  "The offset" should {

    /** Не должен быть меньше нуля */
    "not be allowed to be less than zero" in new LimitOffsetTest {
      assertException(offset = -1)
      assertException(offset = -5)
    }

    /** Может быть больше или равен нулю */
    "be allowed to be greater than or equal to zero" in new LimitOffsetTest {
      assertNoException(offset = 0)
      assertNoException(offset = 1)
      assertNoException(offset = 99)
    }

  }

}
