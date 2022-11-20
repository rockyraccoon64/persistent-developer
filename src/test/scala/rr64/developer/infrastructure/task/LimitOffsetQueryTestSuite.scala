package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.task.LimitOffsetQuery.LimitOffsetException

class LimitOffsetQueryTestSuite
  extends AnyWordSpec
    with Matchers {

  def assertException(limit: Int = 10, offset: Int = 0): Assertion =
    assertThrows[LimitOffsetException] {
      LimitOffsetQuery(limit = limit, offset = offset)
    }

  /** Количество запрашиваемых элементов */
  "The limit" should {

    /** Не должно быть меньше одного */
    "not be allowed to be less than 1" in {
      assertException(limit = 0)
      assertException(limit = -1)
    }

  }

  /** Номер первого запрашиваемого элемента */
  "The offset" should {

    /** Не должен быть меньше нуля */
    "not be allowed to be less than zero" in {
      assertException(offset = -1)
      assertException(offset = -5)
    }

  }

}
