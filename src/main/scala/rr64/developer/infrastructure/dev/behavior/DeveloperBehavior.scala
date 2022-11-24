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
    Behaviors.setup { context =>
      Behaviors.withTimers { timer =>
        implicit val setup: Setup = Setup(workFactor, restFactor, timer, context)
        EventSourcedBehavior[Command, Event, State](
          persistenceId = persistenceId,
          emptyState = State.Free,
          commandHandler = (state, cmd) => {
            context.log.debug("Got command {} in state {}", cmd, state)
            state.applyCommand(cmd)
          },
          eventHandler = (state, evt) => state.applyEvent(evt)
        ).receiveSignal { case (state, RecoveryCompleted) =>
          context.log.info("Developer recovery completed")
          state match {
            case State.Working(currentTask, _) =>
              context.log.info("Starting work timer after recovery for task {}", currentTask)
              startWorkTimer(currentTask)
            case State.Resting(lastCompleted, _) =>
              context.log.info("Starting rest timer after recovery for task {}", lastCompleted)
              startRestTimer(lastCompleted)
            case State.Free =>
          }
        }.withTagger(_ => Set(EventTag))
      }
    }

}
