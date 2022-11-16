package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.Replies.AddTaskResult

import scala.concurrent.duration.DurationInt

object DeveloperBehavior {

  sealed trait Command
  case class AddTask(task: Task, replyTo: ActorRef[AddTaskResult]) extends Command
  private case class FinishTask(task: Task) extends Command
  private case object StopResting extends Command

  sealed trait Event

  case object Event {
    case class TaskStarted(task: Task) extends Event
    case object TaskFinished extends Event
    case object Rested extends Event
  }

  sealed trait State {
    def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State]
    def applyEvent(evt: Event): State
  }

  object State {

    /** Разработчик свободен */
    case object Free extends State {
      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            Effect.persist(Event.TaskStarted(task))
              .thenRun { _: State =>
                val timeNeeded = task.difficulty * setup.timeFactor
                val message = FinishTask(task)
                setup.timer.startSingleTimer(message, timeNeeded.millis) // TODO Не будет выполнено, если упадёт во время работы
              }
              .thenReply(replyTo)(_ => Replies.TaskStarted)
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskStarted(task) => Working(task)
        }
    }

    /** Разработчик работает над задачей */
    case class Working(task: Task) extends State {
      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case FinishTask(task) =>
            Effect.persist(Event.TaskFinished)
              .thenRun {
                case Resting(millis) => setup.timer.startSingleTimer(StopResting, millis.millis)
                case _ =>
              }
          case _ => Effect.stash
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskFinished => Resting(task.difficulty * 100)
        }
    }

    /** Разработчик отдыхает */
    case class Resting(millis: Int) extends State {
      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case StopResting => Effect.persist(Event.Rested)
          case AddTask(task, replyTo) => Effect.stash() // TODO reply
          case _ => Effect.unhandled
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.Rested => State.Free
        }
    }

  }

  object Replies {
    /** Результат добавления задачи */
    sealed trait AddTaskResult
    /** Задача принята в работу */
    case object TaskStarted extends AddTaskResult
  }

  case class Setup(timeFactor: Int, timer: TimerScheduler[Command])

  def apply(persistenceId: PersistenceId, timeFactor: Int): Behavior[Command] =
    Behaviors.withTimers { timer =>
      implicit val setup: Setup = Setup(timeFactor, timer)
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.Free,
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
    }

}
