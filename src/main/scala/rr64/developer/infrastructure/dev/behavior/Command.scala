package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.ActorRef
import rr64.developer.domain.task.Task

import java.util.UUID

/**
 * Команда актору разработчика
 */
sealed trait Command

object Command {

  /**
   * Поручить задачу
   * @param task Новая задача
   * @param replyTo Получатель результата поручения задачи
   * */
  case class AddTask(task: Task, replyTo: ActorRef[Replies.AddTaskResult]) extends Command

  /**
   * Завершить задачу
   * @param id Идентификатор задачи
   * */
  private[behavior] case class FinishTask(id: UUID) extends Command

  /** Завершить отдых */
  private[behavior] case object StopResting extends Command

}
