package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import rr64.developer.domain.dev.{DeveloperReply, DeveloperState}
import rr64.developer.domain.service.DeveloperService
import rr64.developer.domain.task.Difficulty.DifficultyException
import rr64.developer.domain.task.{Task, TaskInfo}
import rr64.developer.infrastructure.api.model.{ApiDeveloperState, ApiError, ApiReply}
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success, Try}

/**
 * REST API сервиса разработки
 * @tparam Query Параметры запроса списка задач
 * @param service Сервис разработки задач
 * @param queryExtractor Парсер запроса списка задач
 */
class RestApi[Query](
  service: DeveloperService[Query],
  queryExtractor: QueryExtractor[Option[String], Query]
) {

  /**
   * Адаптеры DTO
   * */
  private val developerStateAdapter = implicitly[Adapter[DeveloperState, ApiDeveloperState]]
  private val taskInfoAdapter = implicitly[Adapter[TaskInfo, ApiTaskInfo]]
  private val replyAdapter = implicitly[Adapter[DeveloperReply, ApiReply]]
  private val taskToAddAdapter = implicitly[Adapter[ApiTaskToAdd, Task]]

  /** API для команды поручения задачи */
  private val addTaskRoute =
    (path("add-task") & entity(as[ApiTaskToAdd])) { taskToAdd =>
      extractExecutionContext { implicit exec =>
        Try(taskToAddAdapter.convert(taskToAdd)) match {
          case Success(task) =>
            onSuccess(service.addTask(task)) { reply =>
              complete(StatusCodes.Created, replyAdapter.convert(reply))
            }
          case Failure(_: DifficultyException) =>
            complete(StatusCodes.BadRequest, ApiError.TaskDifficulty)
        }
      }
    }

  /** API для запроса состояния разработчика */
  private val developerStateRoute =
    (path("developer-state") & extractExecutionContext) { implicit exec =>
      onSuccess(service.developerState) { value =>
        complete(developerStateAdapter.convert(value))
      }
    }

  /** API для запроса информации о задаче */
  private val taskInfoRoute =
    path("task-info" / JavaUUID) { id =>
      extractExecutionContext { implicit exec =>
        onSuccess(service.taskInfo(id)) {
          case Some(value) => complete(taskInfoAdapter.convert(value))
          case None => complete(StatusCodes.NotFound)
        }
      }
    }

  /** API для запроса списка задач */
  private val taskListRoute =
    (path("task-list") & parameter("query".as[String].optional)) { query =>
      extractExecutionContext { implicit exec =>
        queryExtractor.extract(query) match {
          case Right(parsedQuery) =>
            onSuccess(service.tasks(parsedQuery)) { taskList =>
              complete(taskList.map(taskInfoAdapter.convert))
            }
          case Left(message) =>
            complete(StatusCodes.BadRequest, ApiError.inQuery(message))
        }
      }
    }

  /** Полный API */
  val route: Route = Route.seal(
    pathPrefix("api") {
      (pathPrefix("query") & get) {
        developerStateRoute ~
        taskInfoRoute ~
        taskListRoute
      } ~
      (pathPrefix("command") & post) {
        addTaskRoute
      }
    }
  )

}
