package rr64.developer.infrastructure.task.query

import org.scalamock.clazz.Mock
import org.scalamock.matchers.MockParameter

trait LimitOffsetQueryFactoryTestFacade extends Mock {

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

  def setupFactoryExpectation(factory: LimitOffsetQueryFactory)
      (limit: MockParameter[Int], offset: MockParameter[Int])
      (expected: LimitOffsetQuery): Unit =
    (factory.create _)
      .expects(limit, offset)
      .returning(expected)

}

object LimitOffsetQueryFactoryTestFacade
  extends LimitOffsetQueryFactoryTestFacade