package rr64.developer.infrastructure.state

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.DeveloperBehavior.Event

import scala.concurrent.{ExecutionContext, Future}

class DeveloperStateToRepository(repository: DeveloperStateRepository)
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {
  override def process(envelope: EventEnvelope[Event]): Future[Done] = envelope.event match {
    case Event.TaskStarted(_) =>
      repository.save(envelope.persistenceId, DeveloperState.Working).map(_ => Done)
    case Event.TaskFinished =>
      repository.save(envelope.persistenceId, DeveloperState.Resting).map(_ => Done)
  }
}
