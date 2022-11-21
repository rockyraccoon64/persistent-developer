package rr64.developer.application

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import rr64.developer.domain.Factor
import rr64.developer.infrastructure.RootGuardian
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior

import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.DurationConverters.JavaDurationOps

object Main extends App {

  val config = ConfigFactory.load()
  val appConfig = config.getConfig(ConfigKeys.AppConfig)

  val rootGuardianName = appConfig.getString(ConfigKeys.RootGuardianName)
  val askTimeoutDuration = appConfig.getDuration(ConfigKeys.AskTimeout).toScala
  val developerName = appConfig.getString(ConfigKeys.DeveloperActorName)
  val developerPersistenceId = appConfig.getString(ConfigKeys.DeveloperPersistenceId)
  val workFactor = appConfig.getInt(ConfigKeys.WorkFactor)
  val restFactor = appConfig.getInt(ConfigKeys.RestFactor)

  implicit val system: ActorSystem[RootGuardian.Message] =
    ActorSystem(RootGuardian(), rootGuardianName)
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler
  implicit val askTimeout: Timeout = Timeout(askTimeoutDuration)

  val developerBehavior = DeveloperBehavior(
    persistenceId = PersistenceId.ofUniqueId(developerPersistenceId),
    workFactor = Factor(workFactor),
    restFactor = Factor(restFactor)
  )

  val developerRef = Await.result(
    system.ask(RootGuardian.SpawnDeveloper(developerName, developerBehavior, _)),
    askTimeoutDuration
  )

}
