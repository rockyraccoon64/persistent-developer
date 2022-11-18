package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import rr64.developer.infrastructure.task.TaskWithId

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object DeveloperBehavior {

  private[behavior] case class Setup(workFactor: Int, restFactor: Int, timer: TimerScheduler[Command])

  def apply(persistenceId: PersistenceId, workFactor: Int, restFactor: Int): Behavior[Command] =
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
      }
    }

  def calculateTime(difficulty: Int, factor: Int): FiniteDuration =
    (difficulty * factor).millis

  private def startTimer(
    timer: TimerScheduler[Command],
    message: Command,
    difficulty: Int,
    factor: Int
  ): Unit = {
    val delay = calculateTime(difficulty, factor)
    timer.startSingleTimer(message, delay)
  }

  private[behavior] def startWorkTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.FinishTask(taskWithId.id),
      difficulty = taskWithId.task.difficulty,
      factor = setup.workFactor
    )

  private[behavior] def startRestTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.StopResting,
      difficulty = taskWithId.task.difficulty,
      factor = setup.restFactor
    )


}
