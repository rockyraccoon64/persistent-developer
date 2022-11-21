package rr64.developer.infrastructure.task.query

import rr64.developer.infrastructure.task.query.LimitOffsetQueryFactory.QueryImpl

class LimitOffsetQueryFactory(defaultLimit: Int, maxLimit: Int) {
  require(defaultLimit > 0 && maxLimit > 0 && defaultLimit <= maxLimit)

  val Default: LimitOffsetQuery = create()

  def create(
    limit: Int = defaultLimit,
    offset: Int = 0
  ): LimitOffsetQuery = {
    if (limit > 0 && limit <= maxLimit && offset >= 0)
      QueryImpl(limit, offset)
    else
      throw new LimitOffsetException
  }

}

object LimitOffsetQueryFactory {
  private case class QueryImpl(limit: Int, offset: Int) extends LimitOffsetQuery
}
