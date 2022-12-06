package rr64.developer.infrastructure.task.query

/**
 * Фасад для тестов с использованием
 * параметров запроса limit/offset
 * */
trait LimitOffsetQueryTestFacade
  extends LimitOffsetQueryInstanceTestFacade
    with LimitOffsetQueryFactoryTestFacade
    with LimitOffsetQueryStringExtractorTestFacade

object LimitOffsetQueryTestFacade
  extends LimitOffsetQueryTestFacade
