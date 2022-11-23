package rr64.developer.infrastructure

import akka.NotUsed
import akka.persistence.query.Offset
import akka.projection.eventsourced.EventEnvelope
import akka.projection.testkit.scaladsl.TestSourceProvider
import akka.stream.scaladsl.Source

/**
 * Вспомогательные методы для тестирования проекций
 * */
object ProjectionTestUtils {

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

  /** Тестовый SourceProvider на основе Source */
  def providerFromSource[T](
    source: Source[EventEnvelope[T], NotUsed]
  ): TestSourceProvider[Offset, EventEnvelope[T]] =
    TestSourceProvider(
      source,
      (envelope: EventEnvelope[T]) => envelope.offset
    )

}
