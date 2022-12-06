package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryTestFacade {

  def createQuery(limit: Int, offset: Int): LimitOffsetQuery = {
    val lim = limit
    val off = offset
    new LimitOffsetQuery {
      override def limit: Int = lim
      override def offset: Int = off
    }
  }

  def createFactory(defaultLimit: Int, maxLimit: Int): LimitOffsetQueryFactoryImpl =
    new LimitOffsetQueryFactoryImpl(defaultLimit = defaultLimit, maxLimit = maxLimit)

  def createExtractor(
    factory: LimitOffsetQueryFactory,
    errorMessage: String
  ): LimitOffsetQueryStringExtractor =
    new LimitOffsetQueryStringExtractor(
      factory = factory,
      errorMessage = errorMessage
    )

}

object LimitOffsetQueryTestFacade extends LimitOffsetQueryTestFacade