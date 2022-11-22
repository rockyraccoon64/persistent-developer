package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import rr64.developer.domain.timing.Factor
import rr64.developer.infrastructure.dev.behavior.Timers._

/**
 * Поведение персистентного актора разработчика
 * */
object DeveloperBehavior {

  type DeveloperCommand = Command
  type DeveloperEvent = Event
  type DeveloperRef = ActorRef[Command]

  /** Тэг событий актора разработчика */
  val EventTag = "dev"

  /**
   * Инициализировать поведение разработчика
   * @param persistenceId Persistence ID
   * @param workFactor Рабочий множитель
   * @param restFactor Множитель отдыха
   * */
  def apply(persistenceId: PersistenceId, workFactor: Factor, restFactor: Factor): Behavior[Command] =
    Behaviors.withTimers { timer =>
      implicit val setup: Setup = Setup(workFactor, restFactor, timer)
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.Free,
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      ).receiveSignal {
        case (State.Working(taskWithId, _), RecoveryCompleted) =>
          startWorkTimer(taskWithId)

        case (State.Resting(lastCompleted, _), RecoveryCompleted) =>
          startRestTimer(lastCompleted)
      }.withTagger(_ => Set(EventTag))
    }

}
