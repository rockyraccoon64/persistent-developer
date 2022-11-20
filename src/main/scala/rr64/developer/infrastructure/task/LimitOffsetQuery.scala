package rr64.developer.infrastructure.task

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

object LimitOffsetQuery {

  private val defaultLimit = 20 // TODO app.conf
  private val defaultOffset = 0

  val Default: LimitOffsetQuery = LimitOffsetQuery()

  def apply(
    limit: Int = defaultLimit,
    offset: Int = defaultOffset
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

  class LimitOffsetException extends RuntimeException

}
