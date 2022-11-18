package rr64.developer.infrastructure.api

import akka.http.scaladsl.server.Route
import rr64.developer.domain.DeveloperService

class RestApi(service: DeveloperService) {
  val route: Route = ???
}
