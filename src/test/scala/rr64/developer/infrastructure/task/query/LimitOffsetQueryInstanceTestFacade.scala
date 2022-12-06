package rr64.developer.infrastructure.task.query

/**
 * Фасад для тестов с использованием
 * объектов параметров запроса limit/offset
 */
trait LimitOffsetQueryInstanceTestFacade {

  /** Создать параметры запроса limit/offset */
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