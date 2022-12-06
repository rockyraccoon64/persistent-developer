package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryFactoryTestFacade {

  def createFactory(
    defaultLimit: Int,
    maxLimit: Int
  ): LimitOffsetQueryFactoryImpl =
    new LimitOffsetQueryFactoryImpl(
      defaultLimit = defaultLimit,
      maxLimit = maxLimit
    )

  def createQueryFromFactory(factory: LimitOffsetQueryFactory)
      (limit: Int, offset: Int): LimitOffsetQuery =
    factory.create(limit, offset)

}

object LimitOffsetQueryFactoryTestFacade
  extends LimitOffsetQueryFactoryTestFacade