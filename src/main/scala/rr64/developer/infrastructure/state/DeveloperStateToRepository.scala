package rr64.developer.infrastructure.state

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.infrastructure.DeveloperBehavior.Event

import scala.concurrent.Future

class DeveloperStateToRepository(repository: DeveloperStateRepository) extends Handler[EventEnvelope[Event]] {
  override def process(envelope: EventEnvelope[Event]): Future[Done] = ???
}
