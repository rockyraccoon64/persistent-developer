package rr64.developer.infrastructure.dev

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import rr64.developer.domain.dev.DeveloperState
import rr64.developer.infrastructure.dev.behavior.Event

import scala.concurrent.{ExecutionContext, Future}

/**
 * Обработчик проекции для сохранения состояния разработчика в репозитории
 * @param repository Репозиторий состояний
 * */
class DeveloperStateToRepository(repository: DeveloperStateRepository)
    (implicit ec: ExecutionContext) extends Handler[EventEnvelope[Event]] {

  /** Обработать событие */
  override def process(envelope: EventEnvelope[Event]): Future[Done] = envelope.event match {
    case Event.TaskStarted(_) | Event.Rested(Some(_)) =>
      save(envelope.persistenceId, DeveloperState.Working)
    case Event.TaskFinished(_) =>
      save(envelope.persistenceId, DeveloperState.Resting)
    case Event.Rested(_) =>
      save(envelope.persistenceId, DeveloperState.Free)
    case Event.TaskQueued(_) =>
      Future.successful(Done)
  }

  /**
   * Сохранить состояние разработчика в репозитории
   * @param id Идентификатор разработчика
   * @param state Состояние
   * */
  private def save(id: String, state: DeveloperState): Future[Done] = {
    repository.save(id, state).map(_ => Done)
  }

}
