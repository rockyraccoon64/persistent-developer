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
  private case class FinishTask(id: UUID) extends Command
  private case object StopResting extends Command

  sealed trait Event

  case object Event {
    case class TaskStarted(taskWithId: TaskWithId) extends Event
    case class TaskQueued(taskWithId: TaskWithId) extends Event
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
            val id = generateTaskId()
            val taskWithId = TaskWithId(task, id)
            Effect.persist(Event.TaskStarted(taskWithId))
              .thenRun { _: State =>
                val timeNeeded = task.difficulty * setup.workFactor
                setup.timer.startSingleTimer(FinishTask(id), timeNeeded.millis) // TODO Не будет выполнено, если упадёт во время работы
              }
              .thenReply(replyTo)(_ => Replies.TaskStarted(id))
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskStarted(taskWithId) => Working(taskWithId, Nil)
        }
    }

    /** Разработчик работает над задачей */
    case class Working(currentTask: TaskWithId, taskQueue: Seq[TaskWithId])
      (implicit setup: Setup) extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case FinishTask(id) if id == currentTask.id =>
            Effect.persist(Event.TaskFinished)
              .thenRun {
                case Resting(millis, _) => setup.timer.startSingleTimer(StopResting, millis.millis)
                case _ =>
              }
          case AddTask(task, replyTo) =>
            val id = generateTaskId()
            val taskWithId = TaskWithId(task, id)
            Effect.persist(Event.TaskQueued(taskWithId))
              .thenReply(replyTo)(_ => Replies.TaskQueued(id))
          case _ => Effect.stash
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskFinished => Resting(currentTask.task.difficulty * setup.restFactor, taskQueue)
          case Event.TaskQueued(newTask) => Working(currentTask, taskQueue :+ newTask)
        }
    }

    /** Разработчик отдыхает */
    case class Resting(millis: Int, taskQueue: Seq[TaskWithId])(implicit setup: Setup) extends State {
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
      def task: Task = working.currentTask.task
      def taskId: UUID = working.currentTask.id
    }

  }

  object Replies {
    /** Результат добавления задачи */
    sealed trait AddTaskResult
    /** Задача принята в работу */
    case class TaskStarted(id: UUID) extends AddTaskResult
    /** Задача поставлена в очередь */
    case class TaskQueued(id: UUID) extends AddTaskResult
  }

  case class Setup(workFactor: Int, restFactor: Int, timer: TimerScheduler[Command])

  case class TaskWithId(task: Task, id: UUID)

  private def generateTaskId(): UUID = UUID.randomUUID()

  def apply(persistenceId: PersistenceId, workFactor: Int, restFactor: Int): Behavior[Command] =
    Behaviors.withTimers { timer =>
      implicit val setup: Setup = Setup(workFactor, restFactor, timer)
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State.Free(),
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
    }

}
