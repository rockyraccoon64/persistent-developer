package rr64.developer.infrastructure.task.query

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LimitOffsetQueryStringExtractorTestSuite
  extends AnyWordSpec
  with Matchers {

  trait ExtractorTest {
    private val factory = new LimitOffsetQueryFactory(defaultLimit = 10, maxLimit = 30)
    protected val errorMessage = "Invalid limit + offset"
    protected val extractor = new LimitOffsetQueryStringExtractor(factory, errorMessage)
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

    /** Должен возвращать сообщение об ошибке, когда запрос сформирован некорректно */
    "return an error message when the query format is incorrect" in new ExtractorTest {
      val input = Some("What's this query")
      val result = extractor.extract(input)
      result shouldEqual Left(errorMessage)
    }

  }

}
