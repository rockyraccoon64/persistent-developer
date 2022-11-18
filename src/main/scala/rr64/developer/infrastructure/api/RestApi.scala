package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import rr64.developer.domain.{DeveloperService, DeveloperState, TaskInfo}
import rr64.developer.infrastructure.api.ApiDeveloperState._

import scala.util.{Failure, Success}

class RestApi(service: DeveloperService) {

  private val developerStateAdapter = implicitly[Adapter[DeveloperState, ApiDeveloperState]]
  private val taskInfoAdapter = implicitly[Adapter[TaskInfo, ApiTaskInfo]]

  val route: Route =
    pathPrefix("api") {
      pathPrefix("query") {
        path("developer-state") {
          get {
            extractExecutionContext { implicit exec =>
              onComplete(service.developerState) {
                case Success(value) => complete(developerStateAdapter.convert(value))
                case Failure(exception) => complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        (path("task-info" / JavaUUID) & get) { id =>
          extractExecutionContext { implicit exec =>
            onComplete(service.taskInfo(id)) {
              case Success(value) => complete(value.map(taskInfoAdapter.convert))
              case Failure(exception) => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }

}
