package rr64.developer.infrastructure.dev.behavior

import akka.persistence.typed.scaladsl.Effect
import rr64.developer.domain.Task
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID


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
            .thenRun((_: State) => Timing.startWorkTimer(taskWithId))
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
            .thenRun((_: State) => Timing.startRestTimer(currentTask))

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
            .thenRun((_: State) => taskQueue.headOption.foreach(Timing.startWorkTimer))

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
