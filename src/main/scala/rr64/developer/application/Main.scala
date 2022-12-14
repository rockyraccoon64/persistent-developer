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
import org.slf4j.LoggerFactory
import rr64.developer.domain.dev.Developer
import rr64.developer.domain.service.{DeveloperService, DeveloperServiceFacade}
import rr64.developer.domain.task.Tasks
import rr64.developer.domain.timing.Factor
import rr64.developer.infrastructure.PlainJdbcSession
import rr64.developer.infrastructure.api.{QueryExtractor, RestApi}
import rr64.developer.infrastructure.dev._
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.DeveloperEvent
import rr64.developer.infrastructure.task._
import rr64.developer.infrastructure.task.query.{LimitOffsetQuery, LimitOffsetQueryFactory, LimitOffsetQueryFactoryImpl, LimitOffsetQueryStringExtractor}
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn
import scala.jdk.DurationConverters.JavaDurationOps
import scala.util.{Failure, Success}

/**
 * Точка входа в приложение
 * */
object Main extends App {

  private val log = LoggerFactory.getLogger(getClass)

  /**
   * Чтение файла конфигурации
   * */

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

  /**
   * Инициализация подсистемы акторов
   */

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), rootGuardianName)
  implicit val ec: ExecutionContext = system.executionContext
  implicit val scheduler: Scheduler = system.scheduler
  implicit val askTimeout: Timeout = Timeout(askTimeoutDuration)

  /** Создать актора */
  def spawn[T](behavior: Behavior[T], name: String): ActorRef[T] =
    Await.result(
      system.ask { (replyTo: ActorRef[ActorRef[T]]) =>
        SpawnProtocol.Spawn(behavior, name, Props.empty, replyTo)
      },
      askTimeoutDuration
    )

  /**
   * Инициализация актора разработчика
   * */

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

  val developerRef = spawn(supervisedBehavior, developerName)

  log.info("Developer actor initialized")

  /**
   * Инициализация сервиса разработчика
   * */

  val dbConfig = DatabaseConfig.forConfig[PostgresProfile]("slick")
  val database = dbConfig.db

  val developerStateCodec =
    new DeveloperStateCodec
  val developerStateRepository =
    new DeveloperStateSlickRepository(database, developerStateCodec)
  val developerStateProvider =
    new DeveloperStateFromRepository(developerPersistenceId, developerStateRepository)
  val developer: Developer =
    PersistentDeveloper(developerRef, developerStateProvider)

  log.info("Developer initialized")

  /**
   * Инициализация сервиса задач
   * */

  type Query = LimitOffsetQuery

  val taskStatusCodec =
    new TaskStatusCodec
  val taskRepository: TaskRepository[Query] =
    new TaskSlickRepository(database, taskStatusCodec)
  val tasks: Tasks[Query] =
    new TasksFromRepository[Query](taskRepository)

  val service: DeveloperService[Query] =
    new DeveloperServiceFacade[Query](developer, tasks)

  log.info("Task service initialized")

  /**
   * Инициализация проекций
   * */

  val sourceProvider: SourceProvider[Offset, EventEnvelope[DeveloperEvent]] =
    EventSourcedProvider.eventsByTag[DeveloperEvent](
      system = system,
      readJournalPluginId = JdbcReadJournal.Identifier,
      tag = DeveloperBehavior.EventTag
    )

  val dbUrl = dbConfig.config.getString("db.url")
  val dbUser = dbConfig.config.getString("db.user")
  val dbPassword = dbConfig.config.getString("db.password")

  val jdbcSessionFactory =
    () => new PlainJdbcSession(
      driverClass = "org.postgresql.Driver",
      url = dbUrl,
      user = dbUser,
      password = dbPassword
    )

  val defaultProjectionKey = "0"

  /**
   * Инициализация проекции состояния разработчика
   * */

  val developerStateProjectionHandler =
    new DeveloperStateToRepository(developerStateRepository)

  val developerProjectionName = "dev-projection"

  val developerStateProjection = JdbcProjection.atLeastOnceAsync(
    ProjectionId(developerProjectionName, defaultProjectionKey),
    sourceProvider = sourceProvider,
    sessionFactory = jdbcSessionFactory,
    handler = () => developerStateProjectionHandler
  )

  val developerStateProjectionBehavior: Behavior[ProjectionBehavior.Command] =
    ProjectionBehavior(developerStateProjection)

  val developerStateProjectionRef =
    spawn(developerStateProjectionBehavior, developerProjectionName)

  log.info("Developer state projection initialized")

  /**
   * Инициализация проекции хранилища задач
   * */

  val taskProjectionHandler =
    new TaskToRepository(taskRepository)

  val taskProjectionName = "task-projection"

  val taskProjection = JdbcProjection.atLeastOnceAsync(
    ProjectionId(taskProjectionName, defaultProjectionKey),
    sourceProvider = sourceProvider,
    sessionFactory = jdbcSessionFactory,
    handler = () => taskProjectionHandler
  )

  val taskProjectionBehavior: Behavior[ProjectionBehavior.Command] =
    ProjectionBehavior(taskProjection)

  val taskProjectionRef =
    spawn(taskProjectionBehavior, taskProjectionName)

  log.info("Task projection initialized")

  /**
   * Инициализация REST API
   * */

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
  server.onComplete {
    case Success(binding) =>
      binding.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds)
      log.info("REST API initialized at {}:{}", apiInterface, apiPort)
    case Failure(exception) =>
      log.error(s"Failed to initialize REST API at $apiInterface:$apiPort", exception)
  }

  /**
   * Остановка приложения при нажатии Enter
   * */
  StdIn.readLine()
  system.terminate()

}
