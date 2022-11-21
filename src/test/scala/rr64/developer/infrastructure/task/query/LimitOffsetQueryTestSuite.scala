package rr64.developer.infrastructure.task.query

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LimitOffsetQueryTestSuite
  extends AnyWordSpec
    with Matchers {

  trait LimitOffsetTest {

    private val factory = new LimitOffsetQueryFactory(defaultLimit = 10, maxLimit = 30)

    def assertException(limit: Int = 10, offset: Int = 0): Assertion =
      assertThrows[LimitOffsetException] {
        factory.create(limit = limit, offset = offset)
      }

    def assertNoException(limit: Int = 10, offset: Int = 0): Assertion =
      noException should be thrownBy factory.create(limit = limit, offset = offset)

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
