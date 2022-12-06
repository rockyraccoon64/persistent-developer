package rr64.developer.infrastructure.task.query

import org.scalatest.EitherValues._

trait LimitOffsetQueryStringExtractorTestFacade {

  def createExtractor(
    factory: LimitOffsetQueryFactory,
    errorMessage: String
  ): LimitOffsetQueryStringExtractor =
    new LimitOffsetQueryStringExtractor(
      factory = factory,
      errorMessage = errorMessage
    )

  def extractError(extractor: LimitOffsetQueryStringExtractor)
      (input: String): String =
    extractor.extract(Some(input)).left.value

}

object LimitOffsetQueryStringExtractorTestFacade
  extends LimitOffsetQueryStringExtractorTestFacade
