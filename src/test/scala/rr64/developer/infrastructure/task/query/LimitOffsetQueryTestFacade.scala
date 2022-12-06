package rr64.developer.infrastructure.task.query

trait LimitOffsetQueryTestFacade
  extends LimitOffsetQueryInstanceTestFacade
    with LimitOffsetQueryFactoryTestFacade
    with LimitOffsetQueryExtractorTestFacade

object LimitOffsetQueryTestFacade
  extends LimitOffsetQueryTestFacade
