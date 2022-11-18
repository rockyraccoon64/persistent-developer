package rr64.developer.infrastructure.dev

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import rr64.developer.domain.Task

import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object DeveloperBehavior {

  sealed trait Command
  case class AddTask(task: Task, replyTo: ActorRef[Replies.AddTaskResult]) extends Command
  private case class FinishTask(id: UUID) extends Command
  private case object StopResting extends Command

  sealed trait Event

  case object Event {
    case class TaskStarted(taskWithId: TaskWithId) extends Event
    case class TaskQueued(taskWithId: TaskWithId) extends Event
    case class TaskFinished(taskWithId: TaskWithId) extends Event
    case object Rested extends Event
  }

  sealed trait State {
    def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State]
    def applyEvent(evt: Event)(implicit setup: Setup): State
  }

  object State {

    /** Разработчик свободен */
    case object Free extends State {

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            val taskWithId = createTaskWithId(task)
            Effect.persist(Event.TaskStarted(taskWithId))
              .thenRun((_: State) => startWorkTimer(taskWithId))
              .thenReply(replyTo)(_ => Replies.TaskStarted(taskWithId.id))
          case _ =>
            Effect.unhandled
        }

      override def applyEvent(evt: Event)(implicit setup: Setup): State =
        evt match {
          case Event.TaskStarted(taskWithId) => Working(taskWithId, Nil)
        }

    }

    /** Разработчик работает над задачей */
    case class Working(currentTask: TaskWithId, taskQueue: Seq[TaskWithId]) extends State {

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case FinishTask(id) if id == currentTask.id =>
            Effect.persist(Event.TaskFinished(currentTask))
              .thenRun((_: State) => startRestTimer(currentTask))

          case AddTask(task, replyTo) =>
            val taskWithId = createTaskWithId(task)
            Effect.persist(Event.TaskQueued(taskWithId))
              .thenReply(replyTo)(_ => Replies.TaskQueued(taskWithId.id))

          case _ =>
            Effect.unhandled
        }

      override def applyEvent(evt: Event)(implicit setup: Setup): State =
        evt match {
          case Event.TaskFinished(taskWithId) => Resting(taskWithId, taskQueue)
          case Event.TaskQueued(newTask) => Working(currentTask, taskQueue :+ newTask)
        }

    }

    /** Разработчик отдыхает */
    case class Resting(lastCompleted: TaskWithId, taskQueue: Seq[TaskWithId]) extends State {

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case StopResting =>
            Effect.persist(Event.Rested)
              .thenRun((_: State) => taskQueue.headOption.foreach(startWorkTimer))

          case AddTask(task, replyTo) =>
            val taskWithId = createTaskWithId(task)
            Effect.persist(Event.TaskQueued(taskWithId))
              .thenReply(replyTo)(_ => Replies.TaskQueued(taskWithId.id))

          case _ =>
            Effect.unhandled
        }

      override def applyEvent(evt: Event)(implicit setup: Setup): State =
        evt match {
          case Event.Rested =>
            taskQueue match {
              case head :: tail => Working(head, tail)
              case Nil => State.Free
            }

          case Event.TaskQueued(newTask) =>
            Resting(lastCompleted, taskQueue :+ newTask)
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

  case class TaskWithId(task: Task, id: UUID) // TODO move

  private def generateTaskId(): UUID = UUID.randomUUID()
  private def createTaskWithId(task: Task): TaskWithId =
    TaskWithId(task, generateTaskId())

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

  def calculateTime(difficulty: Int, factor: Int): FiniteDuration = (difficulty * factor).millis

  private def startWorkTimer(taskWithId: TaskWithId)(implicit setup: Setup): Unit = {
    val delay = calculateTime(taskWithId.task.difficulty, setup.workFactor)
    val message = FinishTask(taskWithId.id)
    setup.timer.startSingleTimer(message, delay)
  }

  private def startRestTimer(taskWithId: TaskWithId)(implicit setup: Setup): Unit = {
    val delay = calculateTime(taskWithId.task.difficulty, setup.restFactor)
    setup.timer.startSingleTimer(StopResting, delay)
  }


}
