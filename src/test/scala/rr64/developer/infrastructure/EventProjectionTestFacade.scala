package rr64.developer.infrastructure

import akka.NotUsed
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.{TestProjection, TestSourceProvider}
import akka.stream.scaladsl.Source

trait EventProjectionTestFacade {

  /**
   * Source событий для проекции на основе последовательности событий
   * @param events События
   * @param persistenceId Persistence ID
   * @param startOffset Offset первого события
   * */
  def envelopeSource[T](
    events: Seq[T],
    persistenceId: String,
    startOffset: Long = 0
  ): Source[EventEnvelope[T], NotUsed] =
    Source(events).zipWithIndex.map { case (event, idx) =>
      val offset = startOffset + idx
      EventEnvelope(
        offset = Offset.sequence(offset),
        persistenceId = persistenceId,
        sequenceNr = offset,
        event = event,
        timestamp = offset
      )
    }

  /** Тестовый SourceProvider на основе Source событий */
  def providerFromSource[T](
    source: Source[EventEnvelope[T], NotUsed]
  ): TestSourceProvider[Offset, EventEnvelope[T]] =
    TestSourceProvider(
      source,
      (envelope: EventEnvelope[T]) => envelope.offset
    )

  /** Проекция на основе Source событий */
  def projectionFromEventSource[T](
    handler: Handler[EventEnvelope[T]],
    projectionId: ProjectionId
  )(
    source: Source[EventEnvelope[T], NotUsed]
  ): TestProjection[Offset, EventEnvelope[T]] =
    TestProjection(
      projectionId = projectionId,
      sourceProvider = providerFromSource(source),
      handler = () => handler
    )

  /** Проекция на основе последовательности событий */
  def projectionFromEventSequence[T](
    handler: Handler[EventEnvelope[T]],
    projectionId: ProjectionId
  )(
    events: Seq[T],
    persistenceId: String
  ): TestProjection[Offset, EventEnvelope[T]] = {
    val source = envelopeSource(events, persistenceId)
    projectionFromEventSource(handler, projectionId)(source)
  }

}

object EventProjectionTestFacade
  extends EventProjectionTestFacade