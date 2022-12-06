package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryExtractorTestFacade {

  def createExtractor(
    factory: LimitOffsetQueryFactory,
    errorMessage: String
  ): LimitOffsetQueryStringExtractor =
    new LimitOffsetQueryStringExtractor(
      factory = factory,
      errorMessage = errorMessage
    )

}

object LimitOffsetQueryExtractorTestFacade
  extends LimitOffsetQueryExtractorTestFacade
