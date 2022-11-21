package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.{DeveloperCommand, DeveloperRef}

object RootGuardian {

  trait Message
  case class SpawnDeveloper(
    name: String,
    behavior: Behavior[DeveloperCommand],
    replyTo: ActorRef[DeveloperRef]
  ) extends Message

  def apply(): Behavior[Message] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage {
      case SpawnDeveloper(name, behavior, replyTo) =>
        val ref = ctx.spawn(behavior, name) // TODO supervision
        replyTo ! ref
        Behaviors.empty
    }
  }

}
