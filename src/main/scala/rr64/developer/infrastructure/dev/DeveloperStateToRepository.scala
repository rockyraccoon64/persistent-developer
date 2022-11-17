package rr64.developer.infrastructure.dev

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.dev.DeveloperBehavior.Event

import scala.concurrent.{ExecutionContext, Future}

/**
 * Обработчик проекции для сохранения состояния разработчика в репозитории
 * */
class DeveloperStateToRepository(repository: DeveloperStateRepository)
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {

  /** Обработать событие */
  override def process(envelope: EventEnvelope[Event]): Future[Done] = envelope.event match {
    case Event.TaskStarted(_) =>
      save(envelope.persistenceId, DeveloperState.Working)
    case Event.TaskFinished(_) =>
      save(envelope.persistenceId, DeveloperState.Resting)
    case Event.Rested =>
      save(envelope.persistenceId, DeveloperState.Free)
    case Event.TaskQueued(_) =>
      Future.successful(Done)
  }

  /** Сохранить состояние разработчика в репозитории */
  private def save(id: String, state: DeveloperState): Future[Done] = {
    repository.save(id, state).map(_ => Done)
  }

}
