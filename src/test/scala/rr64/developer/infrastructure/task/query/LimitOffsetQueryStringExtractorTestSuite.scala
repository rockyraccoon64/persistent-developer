package rr64.developer.infrastructure.task.query

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Assertion, EitherValues}
import rr64.developer.infrastructure.task.query.LimitOffsetQueryTestFacade._

/**
 * Тесты парсера параметров запроса limit/offset из строки
 */
class LimitOffsetQueryStringExtractorTestSuite
  extends AnyWordSpec
    with Matchers
    with EitherValues
    with MockFactory {

  /** Фикстура для тестирования парсера */
  private trait ExtractorTest {

    protected val factory = mock[LimitOffsetQueryFactory]
    protected val errorMessage = "Invalid limit + offset"
    protected val extractor = createExtractor(factory, errorMessage)

    /** Проверка на наличие ошибки */
    protected def assertError(query: String): Assertion =
      extractor.extract(Some(query)).left.value shouldEqual errorMessage

  }

  /** Парсер запроса */
  "The query extractor" should {

    /** Должен перенаправлять корректно сформированный запрос фабрике */
    "extract correct queries" in new ExtractorTest {
      val limit = 15
      val offset = 55
      val expected: LimitOffsetQuery = createQuery(limit, offset)
      (factory.create _)
        .expects(limit, offset)
        .returning(expected)

      val input = Some(s"limit:$limit,offset:$offset")
      extractor.extract(input).value shouldEqual expected
    }

    /** Должен возвращать сообщение об ошибке, когда запрос сформирован некорректно */
    "return an error message when the query format is incorrect" in
      new ExtractorTest {
        assertError("What's this query")
      }

    /** Должен возвращать сообщение об ошибке, когда limit больше, чем Integer.MAX_VALUE */
    "return an error message when the limit is greater than Integer.MAX_VALUE" in
      new ExtractorTest {
        assertError("limit:123456789012345,offset:55")
      }

    /** Должен возвращать сообщение об ошибке, когда offset больше, чем Integer.MAX_VALUE */
    "return an error message when the offset is greater than Integer.MAX_VALUE" in
      new ExtractorTest {
        assertError("limit:15,offset:987654321098")
      }

    /** Должен возвращать сообщение об ошибке, когда одно из значений пустое */
    "return an error message when one of the values is not provided" in
      new ExtractorTest {
        assertError("limit:15,offset:")
      }

    /** Должен возвращать сообщение об ошибке, когда запрос пустой */
    "return an error message when the query is empty" in
      new ExtractorTest {
        assertError("")
      }

    /** Должен возвращать значение по умолчанию, когда запрос не передаётся */
    "return the default query when there is no input" in
      new ExtractorTest {
        val default = createQuery(5, 4)
        (factory.default _).expects().returning(default)
        extractor.extract(None).value shouldEqual default
      }

  }

}
