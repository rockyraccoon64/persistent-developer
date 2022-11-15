package rr64.developer.infrastructure

import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.Replies.AddTaskResult

object DeveloperBehavior {

  sealed trait Command
  case class AddTask(task: Task, replyTo: ActorRef[AddTaskResult]) extends Command

  sealed trait Event

  case object Event {
    case object TaskAdded extends Event
  }

  sealed trait State {
    def applyCommand(cmd: Command): Effect[Event, State]
    def applyEvent(evt: Event): State
  }

  object State {

    case object Free extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            Effect.persist(Event.TaskAdded).thenReply(replyTo)(_ => Replies.TaskAdded)
        }

      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskAdded => Working
        }
    }

    case object Working extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] = ???
      override def applyEvent(evt: Event): State = ???
    }

  }

  object Replies {
    sealed trait AddTaskResult
    case object TaskAdded extends AddTaskResult
  }

  def apply(): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("dev"),
      emptyState = State.Free,
      commandHandler = (state, cmd) => state.applyCommand(cmd),
      eventHandler = (state, evt) => state.applyEvent(evt)
    )

}
