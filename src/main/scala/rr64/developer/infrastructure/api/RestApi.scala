package rr64.developer.infrastructure.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import rr64.developer.domain.DeveloperService
import rr64.developer.infrastructure.api.ApiDeveloperState._

import scala.util.{Failure, Success}

class RestApi(service: DeveloperService) {
  val route: Route =
    pathPrefix("api") {
      pathPrefix("query") {
        path("developer-state") {
          get {
            extractExecutionContext { implicit exec =>
              onComplete(service.developerState) {
                case Success(value) => complete(ApiDeveloperState.Working)
                case Failure(exception) => complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }
    }
}
