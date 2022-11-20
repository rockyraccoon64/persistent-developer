package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.task.LimitOffsetQuery.LimitOffsetException

class LimitOffsetQueryTestSuite
  extends AnyWordSpec
    with Matchers {

  /** Количество запрашиваемых элементов */
  "The limit" should {

    /** Не должно быть меньше одного */
    "not be allowed to be less than 1" in {
      assertThrows[LimitOffsetException] {
        LimitOffsetQuery(limit = 0)
      }
      assertThrows[LimitOffsetException] {
        LimitOffsetQuery(limit = -1)
      }
    }
  }

}
