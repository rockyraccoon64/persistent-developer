package rr64.developer.infrastructure.task

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

class LimitOffsetException extends RuntimeException

class LimitOffsetQueryFactory(defaultLimit: Int, maxLimit: Int) {
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