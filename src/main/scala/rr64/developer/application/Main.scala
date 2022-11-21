package rr64.developer.application

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import rr64.developer.domain._
import rr64.developer.infrastructure.api.{QueryExtractor, RestApi}
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.DeveloperRef
import rr64.developer.infrastructure.dev.{DeveloperStateFromRepository, DeveloperStateSlickRepository, PersistentDeveloper}
import rr64.developer.infrastructure.task.query.{LimitOffsetQuery, LimitOffsetQueryFactory, LimitOffsetQueryFactoryImpl, LimitOffsetQueryStringExtractor}
import rr64.developer.infrastructure.task.{TaskRepository, TaskSlickRepository, TasksFromRepository}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.concurrent.duration.DurationInt
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
  val apiInterface = appConfig.getString(ConfigKeys.ApiInterface)
  val apiPort = appConfig.getInt(ConfigKeys.ApiPort)

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), rootGuardianName)
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler
  implicit val askTimeout: Timeout = Timeout(askTimeoutDuration)

  val developerBehavior = DeveloperBehavior(
    persistenceId = PersistenceId.ofUniqueId(developerPersistenceId),
    workFactor = Factor(workFactor),
    restFactor = Factor(restFactor)
  )

  val supervisorStrategy =
    SupervisorStrategy.restartWithBackoff(
      minBackoff = 1.second,
      maxBackoff = 30.seconds,
      randomFactor = 0.2
    )

  val supervisedBehavior =
    Behaviors.supervise(developerBehavior)
      .onFailure(supervisorStrategy)

  val developerRef = Await.result(
    system.ask { (replyTo: ActorRef[DeveloperRef]) =>
      SpawnProtocol.Spawn(supervisedBehavior, developerName, Props.empty, replyTo)
    },
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
  val server = Http().newServerAt(apiInterface, apiPort).bind(restApi.route)
  server.map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

}
