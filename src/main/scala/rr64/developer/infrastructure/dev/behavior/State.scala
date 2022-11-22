package rr64.developer.infrastructure.dev.behavior

import akka.persistence.typed.scaladsl.Effect
import rr64.developer.infrastructure.dev.behavior.Command._
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
        case Event.TaskStarted(taskWithId) =>
          // При получении задачи начинается работа над ней
          Working(taskWithId, Nil)
      }

  }

  /**
   * Разработчик работает над задачей
   * @param currentTask Текущая задача
   * @param taskQueue Очередь задач
   * */
  case class Working(currentTask: TaskWithId, taskQueue: Seq[TaskWithId]) extends State {

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
        case Event.TaskFinished(taskWithId) =>
          // После завершения задачи требуется отдых
          Resting(taskWithId, taskQueue)

        case Event.TaskQueued(newTask) =>
          // Поставив новую задачу в очередь, разработчик продолажет работать над текущей
          Working(currentTask, taskQueue :+ newTask)
      }

  }

  /**
   * Разработчик отдыхает
   * @param lastCompleted Последняя завершённая задача
   * @param taskQueue Очередь задач
   * */
  case class Resting(lastCompleted: TaskWithId, taskQueue: Seq[TaskWithId]) extends State {

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
          // Если в очереди есть задачи, после отдыха начинается работа над первой
          Working(task, taskQueue.tail)

        case Event.Rested(None) =>
          // Если в очереди нет задач, после отдыха разработчик свободен
          State.Free

        case Event.TaskQueued(newTask) =>
          // Поставив новую задачу в очередь, разработчик продолажет отдыхать
          Resting(lastCompleted, taskQueue :+ newTask)
      }

  }

}
