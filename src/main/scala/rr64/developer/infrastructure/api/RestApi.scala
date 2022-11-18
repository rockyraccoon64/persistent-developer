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

  val route: Route = Route.seal(
    pathPrefix("api") {
      (pathPrefix("query") & get) {
        path("developer-state") {
          extractExecutionContext { implicit exec =>
            onComplete(service.developerState) {
              case Success(value) => complete(developerStateAdapter.convert(value))
              case Failure(exception) => complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        path("task-info" / JavaUUID) { id =>
          extractExecutionContext { implicit exec =>
            onComplete(service.taskInfo(id)) {
              case Success(Some(value)) => complete(taskInfoAdapter.convert(value))
              case Success(None) => complete(StatusCodes.NotFound)
              case Failure(exception) => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  )

}
