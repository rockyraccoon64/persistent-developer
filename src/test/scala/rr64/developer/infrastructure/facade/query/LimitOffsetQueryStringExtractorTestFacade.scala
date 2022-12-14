package rr64.developer.infrastructure.facade.query

import org.scalatest.EitherValues._
import rr64.developer.infrastructure.task.query.{LimitOffsetQuery, LimitOffsetQueryFactory, LimitOffsetQueryStringExtractor}

/**
 * Фасад для тестов с использованием
 * парсера параметров запроса
 * limit/offset из строки
 * */
trait LimitOffsetQueryStringExtractorTestFacade {

  /** Создать парсер параметров запроса limit/offset */
  def createExtractor(
    factory: LimitOffsetQueryFactory,
    errorMessage: String
  ): LimitOffsetQueryStringExtractor =
    new LimitOffsetQueryStringExtractor(
      factory = factory,
      errorMessage = errorMessage
    )

  /** Извлечь ошибку при парсинге параметров запроса из строки */
  def extractError(extractor: LimitOffsetQueryStringExtractor)
      (input: String): String =
    extractor.extract(Some(input)).left.value

  /** Успешно извлечь параметры запроса из строки */
  def extractQuerySuccessfully(extractor: LimitOffsetQueryStringExtractor)
      (input: Option[String]): LimitOffsetQuery =
    extractor.extract(input).value

}

object LimitOffsetQueryStringExtractorTestFacade
  extends LimitOffsetQueryStringExtractorTestFacade
