package rr64.developer.application

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Scheduler, _}
import akka.http.scaladsl.Http
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.Offset
import akka.persistence.typed.PersistenceId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.jdbc.scaladsl.JdbcProjection
import akka.projection.scaladsl.SourceProvider
import akka.projection.{ProjectionBehavior, ProjectionId}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import rr64.developer.domain._
import rr64.developer.infrastructure.PlainJdbcSession
import rr64.developer.infrastructure.api.{QueryExtractor, RestApi}
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.{DeveloperEvent, DeveloperRef}
import rr64.developer.infrastructure.dev.{DeveloperStateFromRepository, DeveloperStateSlickRepository, DeveloperStateToRepository, PersistentDeveloper}
import rr64.developer.infrastructure.task.query.{LimitOffsetQuery, LimitOffsetQueryFactory, LimitOffsetQueryFactoryImpl, LimitOffsetQueryStringExtractor}
import rr64.developer.infrastructure.task.{TaskRepository, TaskSlickRepository, TaskToRepository, TasksFromRepository}
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

  val sourceProvider: SourceProvider[Offset, EventEnvelope[DeveloperEvent]] =
    EventSourcedProvider.eventsByTag[DeveloperEvent](
      system = system,
      readJournalPluginId = JdbcReadJournal.Identifier,
      tag = DeveloperBehavior.EventTag
    )

  val postgresDriverClass = "org.postgresql.Driver"
  val dbUrl = dbConfig.config.getString("db.url")
  val dbUser = dbConfig.config.getString("db.user")
  val dbPassword = dbConfig.config.getString("db.password")

  val jdbcSessionFactory =
    () => new PlainJdbcSession(
      driverClass = postgresDriverClass,
      url = dbUrl,
      user = dbUser,
      password = dbPassword
    )

  val developerStateProjectionHandler =
    new DeveloperStateToRepository(developerStateRepository)

  val developerStateProjection = JdbcProjection.atLeastOnceAsync(
    ProjectionId("dev-projection", "0"), // TODO config
    sourceProvider = sourceProvider,
    sessionFactory = jdbcSessionFactory,
    handler = () => developerStateProjectionHandler
  )

  val developerStateProjectionBehavior: Behavior[ProjectionBehavior.Command] =
    ProjectionBehavior(developerStateProjection)

  val developerStateProjectionRef = Await.result(
    system.ask { (replyTo: ActorRef[ActorRef[ProjectionBehavior.Command]]) =>
      SpawnProtocol.Spawn(
        behavior = developerStateProjectionBehavior,
        name = "dev-projection",
        props = Props.empty,
        replyTo = replyTo
      )
    },
    askTimeoutDuration
  )

  val taskProjectionHandler =
    new TaskToRepository(taskRepository)

  val taskProjection = JdbcProjection.atLeastOnceAsync(
    ProjectionId("task-projection", "0"),
    sourceProvider = sourceProvider,
    sessionFactory = jdbcSessionFactory,
    handler = () => taskProjectionHandler
  )

  val taskProjectionBehavior: Behavior[ProjectionBehavior.Command] =
    ProjectionBehavior(taskProjection)

  val taskProjectionRef = Await.result(
    system.ask { (replyTo: ActorRef[ActorRef[ProjectionBehavior.Command]]) =>
      SpawnProtocol.Spawn(
        behavior = taskProjectionBehavior,
        name = "task-projection",
        props = Props.empty,
        replyTo = replyTo
      )
    },
    askTimeoutDuration
  )

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
