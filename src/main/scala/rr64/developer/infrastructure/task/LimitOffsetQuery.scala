package rr64.developer.infrastructure.task

import rr64.developer.infrastructure.task.LimitOffsetQuery.LimitOffsetException

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

object LimitOffsetQuery {
  class LimitOffsetException extends RuntimeException
}

class QueryFactory(defaultLimit: Int, maxLimit: Int) { // TODO app.conf
  require(defaultLimit > 0 && maxLimit > 0 && defaultLimit <= maxLimit)

  val Default: LimitOffsetQuery = create()

  def create(
    limit: Int = defaultLimit,
    offset: Int = 0
  ): LimitOffsetQuery = {
    val lim = limit
    val off = offset
    if (limit > 0 && limit <= maxLimit && offset >= 0)
      new LimitOffsetQuery {
        override def limit: Int = lim
        override def offset: Int = off
      }
    else
      throw new LimitOffsetException
  }

}