package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.Replies.AddTaskResult

import java.util.UUID
import scala.concurrent.duration.DurationInt

object DeveloperBehavior {

  sealed trait Command
  case class AddTask(task: Task, replyTo: ActorRef[AddTaskResult]) extends Command
  private case object FinishTask extends Command
  private case object StopResting extends Command

  sealed trait Event

  case object Event {
    case class TaskStarted(taskWithId: TaskWithId) extends Event
    case object TaskFinished extends Event
    case object Rested extends Event
  }

  sealed trait State {
    def applyCommand(cmd: Command): Effect[Event, State]
    def applyEvent(evt: Event): State
  }

  object State {

    /** Разработчик свободен */
    case class Free()(implicit setup: Setup) extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            val id = UUID.randomUUID()
            val taskWithId = TaskWithId(task, id)
            Effect.persist(Event.TaskStarted(taskWithId))
              .thenRun { _: State =>
                val timeNeeded = task.difficulty * setup.timeFactor
                setup.timer.startSingleTimer(FinishTask, timeNeeded.millis) // TODO Не будет выполнено, если упадёт во время работы
              }
              .thenReply(replyTo)(_ => Replies.TaskStarted(id))
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskStarted(taskWithId) => Working(taskWithId)
        }
    }

    /** Разработчик работает над задачей */
    case class Working(taskWithId: TaskWithId)(implicit setup: Setup) extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case FinishTask =>
            Effect.persist(Event.TaskFinished)
              .thenRun {
                case Resting(millis) => setup.timer.startSingleTimer(StopResting, millis.millis)
                case _ =>
              }
          case _ => Effect.stash
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskFinished => Resting(taskWithId.task.difficulty * setup.restFactor)
        }
    }

    /** Разработчик отдыхает */
    case class Resting(millis: Int)(implicit setup: Setup) extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case StopResting => Effect.persist(Event.Rested)
          case AddTask(task, replyTo) => Effect.stash() // TODO reply
          case _ => Effect.unhandled
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.Rested => State.Free()
        }
    }

    implicit class WorkingOps(working: Working) {
      def task: Task = working.taskWithId.task
      def taskId: UUID = working.taskWithId.id
    }

  }

  object Replies {
    /** Результат добавления задачи */
    sealed trait AddTaskResult
    /** Задача принята в работу */
    case class TaskStarted(id: UUID) extends AddTaskResult
  }

  case class Setup(timeFactor: Int, restFactor: Int, timer: TimerScheduler[Command])

  case class TaskWithId(task: Task, id: UUID)

  def apply(persistenceId: PersistenceId, timeFactor: Int, restFactor: Int): Behavior[Command] =
    Behaviors.withTimers { timer =>
      implicit val setup: Setup = Setup(timeFactor, restFactor, timer)
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.Free(),
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
    }

}
