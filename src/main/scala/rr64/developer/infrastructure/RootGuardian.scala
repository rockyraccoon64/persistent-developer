package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.{DeveloperCommand, DeveloperRef}

import scala.concurrent.duration.DurationInt

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
        val strategy = SupervisorStrategy.restartWithBackoff(
          minBackoff = 1.second,
          maxBackoff = 30.seconds,
          randomFactor = 0.2
        )
        val supervisedBehavior =
          Behaviors.supervise(behavior).onFailure(strategy)
        val ref = ctx.spawn(supervisedBehavior, name)
        replyTo ! ref
        Behaviors.empty
    }
  }

}
