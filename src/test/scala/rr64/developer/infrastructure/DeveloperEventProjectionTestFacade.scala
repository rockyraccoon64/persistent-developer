package rr64.developer.infrastructure

import akka.persistence.query.Offset
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.TestProjection

trait DeveloperEventProjectionTestFacade
  extends EventProjectionTestFacade
    with DeveloperEventTestFacade {

  type ProjHandler = Handler[EventEnvelope[Event]]
  type TestProj = TestProjection[Offset, EventEnvelope[Event]]

}

object DeveloperEventProjectionTestFacade
  extends DeveloperEventProjectionTestFacade