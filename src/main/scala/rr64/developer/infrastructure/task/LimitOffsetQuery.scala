package rr64.developer.infrastructure.task

case class LimitOffsetQuery(limit: Int = 20, offset: Int = 0)

object LimitOffsetQuery {
  val Default: LimitOffsetQuery = LimitOffsetQuery() // TODO Ограничения
}
