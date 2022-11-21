package rr64.developer.application

import akka.actor.typed.{ActorSystem, Scheduler}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import rr64.developer.domain.Factor
import rr64.developer.infrastructure.RootGuardian
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object Main extends App {
  implicit val system: ActorSystem[RootGuardian.Message] =
    ActorSystem(RootGuardian(), "root-guardian")
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler

  implicit val timeout: Timeout = Timeout(5.seconds) // TODO config

  val developerName = "dev-actor"
  val developerBehavior = DeveloperBehavior(
    persistenceId = PersistenceId.of("dev", "00"),
    workFactor = Factor(10), // TODO config
    restFactor = Factor(5)
  )
  val developerRef = system.ask { replyTo =>
    RootGuardian.SpawnDeveloper(developerName, developerBehavior, replyTo)
  }
}
