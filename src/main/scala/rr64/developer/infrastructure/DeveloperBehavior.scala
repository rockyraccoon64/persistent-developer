package rr64.developer.infrastructure

import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.Replies.{AddTaskResult, TaskAdded}

object DeveloperBehavior {

  sealed trait Command
  case class AddTask(task: Task, replyTo: ActorRef[AddTaskResult]) extends Command

  sealed trait Event

  sealed trait State {
    def applyCommand(cmd: Command): Effect[Event, State]
  }

  case object Free extends State {
    override def applyCommand(cmd: Command): Effect[Event, State] =
      cmd match {
        case AddTask(task, replyTo) =>
          Effect.reply(replyTo)(TaskAdded)
      }
  }

  object Replies {
    sealed trait AddTaskResult
    case object TaskAdded extends AddTaskResult
  }

  def apply(): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("dev"),
      emptyState = Free,
      commandHandler = (state, cmd) => state.applyCommand(cmd),
      eventHandler = (state, evt) => state
    )
}
