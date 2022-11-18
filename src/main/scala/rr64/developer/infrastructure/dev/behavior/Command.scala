package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.ActorRef
import rr64.developer.domain.Task

import java.util.UUID

/**
 * Команда актору разработчика
 */
sealed trait Command

object Command {
  /** Добавить задачу */
  case class AddTask(task: Task, replyTo: ActorRef[Replies.AddTaskResult]) extends Command
  /** Завершить задачу */
  private[behavior] case class FinishTask(id: UUID) extends Command
  /** Завершить отдых */
  private[behavior] case object StopResting extends Command
}
