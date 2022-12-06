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

}

object LimitOffsetQueryFactoryTestFacade
  extends LimitOffsetQueryFactoryTestFacade