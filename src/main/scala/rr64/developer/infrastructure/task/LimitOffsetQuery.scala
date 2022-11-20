package rr64.developer.infrastructure.task

import rr64.developer.infrastructure.task.LimitOffsetQuery.LimitOffsetException

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

object LimitOffsetQuery {
  class LimitOffsetException extends RuntimeException
}

class QueryFactory(defaultLimit: Int) { // TODO app.conf

  val Default: LimitOffsetQuery = create()

  def create(
    limit: Int = defaultLimit,
    offset: Int = 0
  ): LimitOffsetQuery = {
    val lim = limit
    val off = offset
    if (limit > 0 && offset >= 0)
      new LimitOffsetQuery {
        override def limit: Int = lim
        override def offset: Int = off
      }
    else
      throw new LimitOffsetException
  }

}