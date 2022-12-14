package rr64.developer.infrastructure.facade.query

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
