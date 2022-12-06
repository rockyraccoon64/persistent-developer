package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryStringExtractorTestFacade {

  def createExtractor(
    factory: LimitOffsetQueryFactory,
    errorMessage: String
  ): LimitOffsetQueryStringExtractor =
    new LimitOffsetQueryStringExtractor(
      factory = factory,
      errorMessage = errorMessage
    )

}

object LimitOffsetQueryStringExtractorTestFacade
  extends LimitOffsetQueryStringExtractorTestFacade
