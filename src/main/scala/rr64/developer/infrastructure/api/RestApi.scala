package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import rr64.developer.domain.Difficulty.DifficultyException
import rr64.developer.domain._
import rr64.developer.infrastructure.api.ApiDeveloperState._
import spray.json.DefaultJsonProtocol._

/**
 * REST API сервиса разработки
 */
class RestApi[Query](service: DeveloperService[Query], extractQuery: Option[String] => Query) {

  private val developerStateAdapter = implicitly[Adapter[DeveloperState, ApiDeveloperState]]
  private val taskInfoAdapter = implicitly[Adapter[TaskInfo, ApiTaskInfo]]
  private val replyAdapter = implicitly[Adapter[DeveloperReply, ApiReply]]

  private val developerStateRoute =
    (path("developer-state") & extractExecutionContext) { implicit exec =>
      onSuccess(service.developerState) { value =>
        complete(developerStateAdapter.convert(value))
      }
    }

  private val taskInfoRoute =
    path("task-info" / JavaUUID) { id =>
      extractExecutionContext { implicit exec =>
        onSuccess(service.taskInfo(id)) {
          case Some(value) => complete(taskInfoAdapter.convert(value))
          case None => complete(StatusCodes.NotFound)
        }
      }
    }

  private val addTaskRoute =
    (path("add-task") & entity(as[ApiTaskToAdd])) { taskToAdd =>
      extractExecutionContext { implicit exec =>
        val task = Task(taskToAdd.difficulty)
        onSuccess(service.addTask(task)) { reply =>
          complete(StatusCodes.Created, replyAdapter.convert(reply))
        }
      }
    }

  private val taskListRoute =
    (path("task-list") & parameter("query".as[String].optional)) { query =>
      extractExecutionContext { implicit exec =>
        val parsedQuery = extractQuery(query)
        onSuccess(service.tasks(parsedQuery)) { taskList =>
          complete(taskList.map(taskInfoAdapter.convert))
        }
      }
    }

  private implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: DifficultyException =>
        extractUri { _ =>
          complete(StatusCodes.BadRequest, ApiError.TaskDifficulty)
        }
    }

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
