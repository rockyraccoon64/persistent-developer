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

    /** Разработчик свободен */
    case object Free extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] =
        cmd match {
          case AddTask(task, replyTo) =>
            Effect.persist(Event.TaskAdded).thenReply(replyTo)(_ => Replies.TaskStarted)
        }
      override def applyEvent(evt: Event): State =
        evt match {
          case Event.TaskAdded => Working
        }
    }

    /** Разработчик работает над задачей */
    case object Working extends State {
      override def applyCommand(cmd: Command): Effect[Event, State] = ???
      override def applyEvent(evt: Event): State = ???
    }

  }

  object Replies {
    /** Результат добавления задачи */
    sealed trait AddTaskResult
    /** Задача принята в работу */
    case object TaskStarted extends AddTaskResult
  }

  def apply(): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("dev"),
      emptyState = State.Free,
      commandHandler = (state, cmd) => state.applyCommand(cmd),
      eventHandler = (state, evt) => state.applyEvent(evt)
    )

}
