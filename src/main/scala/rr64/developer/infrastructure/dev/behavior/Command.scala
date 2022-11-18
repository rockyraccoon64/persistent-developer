package rr64.developer.infrastructure.dev.behavior

import akka.actor.typed.ActorRef
import rr64.developer.domain.Task

import java.util.UUID

sealed trait Command

object Command {
  case class AddTask(task: Task, replyTo: ActorRef[Replies.AddTaskResult]) extends Command
  private[behavior] case class FinishTask(id: UUID) extends Command
  private[behavior] case object StopResting extends Command
}
