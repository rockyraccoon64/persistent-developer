package rr64.developer.infrastructure.task.query

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}
