package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryTestFacade {
  def createFactory(defaultLimit: Int, maxLimit: Int): LimitOffsetQueryFactoryImpl =
    new LimitOffsetQueryFactoryImpl(defaultLimit = defaultLimit, maxLimit = maxLimit)
}

object LimitOffsetQueryTestFacade extends LimitOffsetQueryTestFacade