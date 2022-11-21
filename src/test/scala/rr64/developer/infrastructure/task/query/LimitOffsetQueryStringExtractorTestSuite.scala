package rr64.developer.infrastructure.task.query

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LimitOffsetQueryStringExtractorTestSuite
  extends AnyWordSpec
    with Matchers
    with EitherValues {

  trait ExtractorTest {
    private val factory = new LimitOffsetQueryFactory(defaultLimit = 10, maxLimit = 30)
    protected val errorMessage = "Invalid limit + offset"
    protected val extractor = new LimitOffsetQueryStringExtractor(factory, errorMessage)
  }

  /** Парсер запроса */
  "The query extractor" should {

    /** Должен парсить корректно сформированный запрос */
    "extract correct queries" in new ExtractorTest {
      val input = Some("limit:15,offset:55")
      val result = extractor.extract(input).right.value
      result.limit shouldEqual 15
      result.offset shouldEqual 55
    }

    /** Должен возвращать сообщение об ошибке, когда запрос сформирован некорректно */
    "return an error message when the query format is incorrect" in new ExtractorTest {
      val input = Some("What's this query")
      val result = extractor.extract(input)
      result.left.value shouldEqual errorMessage
    }

    /** Должен возвращать сообщение об ошибке, когда limit больше, чем Integer.MAX_VALUE */
    "return an error message when the limit is greater than Integer.MAX_VALUE" in new ExtractorTest {
      val input = Some("limit:123456789012345,offset:55")
      val result = extractor.extract(input)
      result.left.value shouldEqual errorMessage
    }

    /** Должен возвращать сообщение об ошибке, когда offset больше, чем Integer.MAX_VALUE */
    "return an error message when the offset is greater than Integer.MAX_VALUE" in new ExtractorTest {
      val input = Some("limit:15,offset:987654321098")
      val result = extractor.extract(input)
      result.left.value shouldEqual errorMessage
    }

    /** Должен возвращать сообщение об ошибке, когда одно из значений пустое */
    "return an error message when one of the values is not provided" in new ExtractorTest {
      val input = Some("limit:15,offset:")
      val result = extractor.extract(input)
      result.left.value shouldEqual errorMessage
    }

  }

}
