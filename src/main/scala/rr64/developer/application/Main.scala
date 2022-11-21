package rr64.developer.application

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import rr64.developer.domain.Factor
import rr64.developer.infrastructure.RootGuardian
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior

import scala.concurrent.ExecutionContext
import scala.jdk.DurationConverters.JavaDurationOps

object Main extends App {

  implicit val system: ActorSystem[RootGuardian.Message] =
    ActorSystem(RootGuardian(), "root-guardian")
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler

  val config = ConfigFactory.load()
  val appConfig = config.getConfig(ConfigKeys.AppConfig)

  val timeoutDuration = appConfig.getDuration(ConfigKeys.AskTimeout).toScala

  implicit val timeout: Timeout = Timeout(timeoutDuration)

  val developerName = appConfig.getString(ConfigKeys.DeveloperActorName)
  val developerBehavior = DeveloperBehavior(
    persistenceId = PersistenceId.ofUniqueId(ConfigKeys.DeveloperPersistenceId),
    workFactor = Factor(10), // TODO config
    restFactor = Factor(5)
  )
  val developerRef = system.ask { replyTo =>
    RootGuardian.SpawnDeveloper(developerName, developerBehavior, replyTo)
  }
}
