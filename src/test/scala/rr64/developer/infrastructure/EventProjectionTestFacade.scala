package rr64.developer.infrastructure

import akka.NotUsed
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.TestProjection
import akka.stream.scaladsl.Source
import rr64.developer.infrastructure.dev.behavior.Event

trait EventProjectionTestFacade {

  /** Проекция на основе Source событий */
  def projectionFromSource(
    handler: Handler[EventEnvelope[Event]],
    projectionId: ProjectionId
  )(
    source: Source[EventEnvelope[Event], NotUsed]
  ): TestProjection[Offset, EventEnvelope[Event]] =
    TestProjection(
      projectionId = projectionId,
      sourceProvider = ProjectionTestUtils.providerFromSource(source),
      handler = () => handler
    )

}

object EventProjectionTestFacade
  extends EventProjectionTestFacade