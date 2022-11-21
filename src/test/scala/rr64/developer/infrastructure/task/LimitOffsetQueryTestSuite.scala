package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LimitOffsetQueryTestSuite
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

  trait LimitOffsetTest {

    private val factory = new LimitOffsetQueryFactory(defaultLimit = 10, maxLimit = 30)

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

  class ExtractorTest {
    private val factory = new LimitOffsetQueryFactory(defaultLimit = 10, maxLimit = 30)
    protected val extractor = new LimitOffsetQueryStringExtractor(factory)
  }

  /** Парсер запроса */
  "The query extractor" should {

    /** Должен парсить корректно сформированный запрос */
    "extract correct queries" in new ExtractorTest {
      val input = Some("limit:10,offset:55")
      val result = extractor.extract(input)
      result.map { query =>
        query.limit shouldEqual 10
        query.offset shouldEqual 55
      }
    }

  }

}
