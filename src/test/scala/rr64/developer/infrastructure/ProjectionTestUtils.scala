package rr64.developer.infrastructure

import akka.NotUsed
import akka.persistence.query.Offset
import akka.projection.eventsourced.EventEnvelope
import akka.stream.scaladsl.Source

object ProjectionTestUtils {

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
}
