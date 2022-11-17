package rr64.developer.infrastructure.state

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.DeveloperBehavior.Event

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
    case Event.TaskFinished =>
      save(envelope.persistenceId, DeveloperState.Resting)
  }

  /** Сохранить состояние разработчика в репозитории */
  private def save(id: String, state: DeveloperState): Future[Done] = {
    repository.save(id, state).map(_ => Done)
  }

}
