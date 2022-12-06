package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryInstanceTestFacade {

  def createQuery(limit: Int, offset: Int): LimitOffsetQuery = {
    val lim = limit
    val off = offset
    new LimitOffsetQuery {
      override def limit: Int = lim
      override def offset: Int = off
    }
  }

}

object LimitOffsetQueryInstanceTestFacade
  extends LimitOffsetQueryInstanceTestFacade