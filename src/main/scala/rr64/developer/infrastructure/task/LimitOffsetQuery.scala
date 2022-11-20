package rr64.developer.infrastructure.task

case class LimitOffsetQuery private(limit: Int, offset: Int)

object LimitOffsetQuery {

  private val defaultLimit = 20 // TODO app.conf
  private val defaultOffset = 0

  val Default: LimitOffsetQuery = LimitOffsetQuery()

  def apply(
    limit: Int = defaultLimit,
    offset: Int = defaultOffset
  ): LimitOffsetQuery =
    if (limit > 0 && offset >= 0)
      new LimitOffsetQuery(limit, offset)
    else
      throw new LimitOffsetException

  class LimitOffsetException extends RuntimeException

}
