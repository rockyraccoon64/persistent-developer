package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryTestFacade {

  def createQuery(limit: Int, offset: Int): LimitOffsetQuery = {
    val lim = limit
    val off = offset
    new LimitOffsetQuery {
      override def limit: Int = lim
      override def offset: Int = off
    }
  }

}

object LimitOffsetQueryTestFacade
  extends LimitOffsetQueryTestFacade