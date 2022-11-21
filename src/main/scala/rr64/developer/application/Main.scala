package rr64.developer.application

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import rr64.developer.domain.{Developer, DeveloperService, DeveloperServiceFacade, Factor, Tasks}
import rr64.developer.infrastructure.RootGuardian
import rr64.developer.infrastructure.api.{QueryExtractor, RestApi}
import rr64.developer.infrastructure.dev.{DeveloperStateFromRepository, DeveloperStateSlickRepository, PersistentDeveloper}
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior
import rr64.developer.infrastructure.task.{TaskRepository, TaskSlickRepository, TasksFromRepository}
import rr64.developer.infrastructure.task.query.{LimitOffsetQuery, LimitOffsetQueryFactory, LimitOffsetQueryFactoryImpl, LimitOffsetQueryStringExtractor}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

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
  val defaultLimit = appConfig.getInt(ConfigKeys.DefaultLimit)
  val maxLimit = appConfig.getInt(ConfigKeys.MaxLimit)

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

  type Query = LimitOffsetQuery

  val dbConfig = DatabaseConfig.forConfig[PostgresProfile]("slick")
  val database = dbConfig.db

  val developerStateRepository =
    new DeveloperStateSlickRepository(database)
  val developerStateProvider =
    new DeveloperStateFromRepository(developerPersistenceId, developerStateRepository)
  val developer: Developer =
    PersistentDeveloper(developerRef, developerStateProvider)

  val taskRepository: TaskRepository[Query] =
    new TaskSlickRepository(database)
  val tasks: Tasks[Query] =
    new TasksFromRepository[Query](taskRepository)
  val service: DeveloperService[Query] =
    new DeveloperServiceFacade[Query](developer, tasks)

  val queryFactory: LimitOffsetQueryFactory =
    new LimitOffsetQueryFactoryImpl(
      defaultLimit = defaultLimit,
      maxLimit = maxLimit
    )
  val queryExtractorErrorMessage: String =
    "Invalid list query parameters"
  val queryExtractor: QueryExtractor[Option[String], Query] =
    new LimitOffsetQueryStringExtractor(queryFactory, queryExtractorErrorMessage)

  val restApi = new RestApi[Query](service, queryExtractor)

}
