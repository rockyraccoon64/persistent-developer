package rr64.developer.infrastructure.task.query

import org.scalamock.clazz.Mock
import org.scalamock.matchers.MockParameter

/**
 * Фасад для тестов с использованием
 * фабрики параметров запроса limit/offset
 */
trait LimitOffsetQueryFactoryTestFacade extends Mock {

  /** Создать фабрику */
  def createFactory(
    defaultLimit: Int,
    maxLimit: Int
  ): LimitOffsetQueryFactoryImpl =
    new LimitOffsetQueryFactoryImpl(
      defaultLimit = defaultLimit,
      maxLimit = maxLimit
    )

  /** Создать параметры запроса с использованием фабрики */
  def createQueryFromFactory(factory: LimitOffsetQueryFactory)
      (limit: Int, offset: Int): LimitOffsetQuery =
    factory.create(limit, offset)

  /** Установить поведение заглушки фабрики параметров запроса */
  def setupFactoryExpectation(factory: LimitOffsetQueryFactory)
      (limit: MockParameter[Int], offset: MockParameter[Int])
      (expected: LimitOffsetQuery): Unit =
    (factory.create _)
      .expects(limit, offset)
      .returning(expected)

  /** Установить заглушку значения по умолчанию
   * в фабрике параметров запроса */
  def setupFactoryDefaultExpectation(factory: LimitOffsetQueryFactory)
      (result: LimitOffsetQuery): Unit =
    (factory.default _)
      .expects()
      .returning(result)

}

object LimitOffsetQueryFactoryTestFacade
  extends LimitOffsetQueryFactoryTestFacade