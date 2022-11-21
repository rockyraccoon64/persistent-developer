package rr64.developer.infrastructure.dev.behavior

import akka.persistence.typed.scaladsl.Effect
import rr64.developer.infrastructure.task.TaskWithId

/**
 * Состояние актора разработчика
 */
sealed trait State {
  /** Обработать команду */
  def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State]
  /** Обработать событие */
  def applyEvent(evt: Event)(implicit setup: Setup): State
}

object State {

  /** Разработчик свободен */
  case object Free extends State {
    import Command._

    override def applyCommand(cmd: Command)(implicit setup: Setup): Effect[Event, State] =
      cmd match {
        case AddTask(task, replyTo) =>
          // Начать работу над задачей
          val taskWithId = TaskWithId.fromTask(task)
          Effect.persist(Event.TaskStarted(taskWithId))
            .thenRun((_: State) => Timers.startWorkTimer(taskWithId))
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
          // Завершить работу над задачей
          Effect.persist(Event.TaskFinished(currentTask))
            .thenRun((_: State) => Timers.startRestTimer(currentTask))

        case AddTask(task, replyTo) =>
          // Поставить задачу в очередь
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
          // Завершить отдых
          Effect.persist(Event.Rested(taskQueue.headOption))
            .thenRun((_: State) => taskQueue.headOption.foreach(Timers.startWorkTimer))

        case AddTask(task, replyTo) =>
          // Поставить задачу в очередь
          val taskWithId = TaskWithId.fromTask(task)
          Effect.persist(Event.TaskQueued(taskWithId))
            .thenReply(replyTo)(_ => Replies.TaskQueued(taskWithId.id))

        case _ =>
          Effect.unhandled
      }

    override def applyEvent(evt: Event)(implicit setup: Setup): State =
      evt match {
        case Event.Rested(Some(task)) if taskQueue.headOption.contains(task) =>
          Working(task, taskQueue.tail)

        case Event.Rested(None) =>
          State.Free

        case Event.TaskQueued(newTask) =>
          Resting(lastCompleted, taskQueue :+ newTask)
      }

  }

}
