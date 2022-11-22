package rr64.developer.infrastructure.task.query

/**
 * Параметры запроса списка с использованием limit/offset
 * */
trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}
