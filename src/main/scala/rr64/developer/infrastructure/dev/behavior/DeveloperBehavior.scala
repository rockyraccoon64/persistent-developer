package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import rr64.developer.domain.Task
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object DeveloperBehavior {

  sealed trait State {
    def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State]
    def applyEvent(evt: Event)(implicit setup: Setup): State
  }

  object State {

    /** Разработчик свободен */
    case object Free extends State {
      import Command._

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            val taskWithId = TaskWithId.fromTask(task)
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
      import Command._

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case FinishTask(id) if id == currentTask.id =>
            Effect.persist(Event.TaskFinished(currentTask))
              .thenRun((_: State) => startRestTimer(currentTask))

          case AddTask(task, replyTo) =>
            val taskWithId = TaskWithId.fromTask(task)
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
      import Command._

      override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
        cmd match {
          case StopResting =>
            Effect.persist(Event.Rested)
              .thenRun((_: State) => taskQueue.headOption.foreach(startWorkTimer))

          case AddTask(task, replyTo) =>
            val taskWithId = TaskWithId.fromTask(task)
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

  private case class Setup(workFactor: Int, restFactor: Int, timer: TimerScheduler[Command])

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

  private def startWorkTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.FinishTask(taskWithId.id),
      difficulty = taskWithId.task.difficulty,
      factor = setup.workFactor
    )

  private def startRestTimer(taskWithId: TaskWithId)
      (implicit setup: Setup): Unit =
    startTimer(
      timer = setup.timer,
      message = Command.StopResting,
      difficulty = taskWithId.task.difficulty,
      factor = setup.restFactor
    )


}
