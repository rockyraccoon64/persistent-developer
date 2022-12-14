package rr64.developer.infrastructure.facade.event

import akka.persistence.query.Offset
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.TestProjection

/**
 * Тестовый фасад для проекций событий разработчика
 * */
trait DeveloperEventProjectionTestFacade
  extends EventProjectionTestFacade
    with DeveloperEventTestFacade {

  /** Обработчик проекции */
  type ProjHandler = Handler[EventEnvelope[Event]]

  /** Тестовая проекция */
  type TestProj = TestProjection[Offset, EventEnvelope[Event]]

}

object DeveloperEventProjectionTestFacade
  extends DeveloperEventProjectionTestFacade