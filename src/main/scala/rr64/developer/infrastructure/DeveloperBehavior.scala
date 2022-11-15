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

  sealed trait Event

  case object Event {
    case class TaskAdded(task: Task) extends Event
    case class TaskFinished(task: Task) extends Event
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
            Effect.persist(Event.TaskAdded(task))
              .thenRun { _: State =>
                setup.timer.startSingleTimer(FinishTask(task), (task.difficulty * 10).millis) // TODO Factor
              }
              .thenReply(replyTo)(_ => Replies.TaskStarted)
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskAdded(task) => Working(task)
        }
    }

    /** Разработчик работает над задачей */
    case class Working(task: Task) extends State {
      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case FinishTask(task) =>
            Effect.persist(Event.TaskFinished(task))
          case _ => Effect.stash
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskFinished(_) => Free
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

  def apply(persistenceId: PersistenceId): Behavior[Command] =
    Behaviors.withTimers { timer =>
      implicit val setup: Setup = Setup(10, timer)
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.Free,
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
    }

}
