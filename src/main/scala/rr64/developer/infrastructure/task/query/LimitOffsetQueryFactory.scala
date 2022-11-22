package rr64.developer.infrastructure.task.query

/** Фабрика параметров запроса с использованием limit/offset */
trait LimitOffsetQueryFactory {

  /** Параметры по умолчанию */
  def default: LimitOffsetQuery

  /** Создать параметры запроса */
  def create(limit: Int, offset: Int): LimitOffsetQuery

}

/**
 * Реализация фабрики параметров запроса с использованием limit/offset
 * @param defaultLimit Limit по умолчанию
 * @param maxLimit Максимальное значение limit
 * */
class LimitOffsetQueryFactoryImpl(defaultLimit: Int, maxLimit: Int)
    extends LimitOffsetQueryFactory {
  require(defaultLimit > 0 && maxLimit > 0 && defaultLimit <= maxLimit)

  import rr64.developer.infrastructure.task.query.LimitOffsetQueryFactoryImpl.QueryImpl

  override lazy val default: LimitOffsetQuery =
    create(limit = defaultLimit, offset = 0)

  override def create(limit: Int, offset: Int): LimitOffsetQuery =
    if (limit > 0 && limit <= maxLimit && offset >= 0)
      QueryImpl(limit, offset)
    else
      throw new LimitOffsetException

}

object LimitOffsetQueryFactoryImpl {

  /** Реализация параметров запроса с использованием limit/offset */
  private case class QueryImpl(limit: Int, offset: Int) extends LimitOffsetQuery

}
