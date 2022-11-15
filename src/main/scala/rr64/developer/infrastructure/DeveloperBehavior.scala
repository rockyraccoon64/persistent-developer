package rr64.developer.infrastructure

import akka.actor.typed.ActorSystem
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object DeveloperBehavior {

  sealed trait Command

  sealed trait Event

  sealed trait State
  case object Free extends State

  def apply(system: ActorSystem[_]): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("dev"),
      emptyState = Free,
      commandHandler = (state, cmd) => Effect.none,
      eventHandler = (state, evt) => state
    )
}
