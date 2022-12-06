package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryTestFacade
  extends LimitOffsetQueryInstanceTestFacade
    with LimitOffsetQueryFactoryTestFacade
    with LimitOffsetQueryStringExtractorTestFacade

object LimitOffsetQueryTestFacade
  extends LimitOffsetQueryTestFacade
