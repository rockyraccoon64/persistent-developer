package rr64.developer.infrastructure.task.query

trait AbstractLimitOffsetQueryFactory {
  def default: LimitOffsetQuery
  def create(limit: Int, offset: Int): LimitOffsetQuery
}

class LimitOffsetQueryFactoryImpl(defaultLimit: Int, maxLimit: Int)
    extends AbstractLimitOffsetQueryFactory {
  require(defaultLimit > 0 && maxLimit > 0 && defaultLimit <= maxLimit)

  import rr64.developer.infrastructure.task.query.LimitOffsetQueryFactoryImpl.QueryImpl

  private lazy val _default = create(limit = defaultLimit, offset = 0)

  override def default: LimitOffsetQuery = _default

  override def create(limit: Int, offset: Int): LimitOffsetQuery =
    if (limit > 0 && limit <= maxLimit && offset >= 0)
      QueryImpl(limit, offset)
    else
      throw new LimitOffsetException

}

object LimitOffsetQueryFactoryImpl {
  private case class QueryImpl(limit: Int, offset: Int) extends LimitOffsetQuery
}
