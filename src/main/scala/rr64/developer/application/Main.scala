package rr64.developer.application

import akka.actor.typed.ActorSystem
import rr64.developer.infrastructure.RootGuardian

object Main extends App {
  implicit val system: ActorSystem[RootGuardian.Message] =
    ActorSystem(RootGuardian(), "root-guardian")
}
