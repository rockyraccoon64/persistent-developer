package rr64.developer.infrastructure.api

import rr64.developer.domain.DeveloperState
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiDeveloperState private(state: String)

object ApiDeveloperState {

  val Free: ApiDeveloperState = ApiDeveloperState("Free")
  val Working: ApiDeveloperState = ApiDeveloperState("Working")
  val Resting: ApiDeveloperState = ApiDeveloperState("Resting")

  implicit val developerStateJsonFormat: RootJsonFormat[ApiDeveloperState] =
    jsonFormat1(ApiDeveloperState.apply)

  implicit val adapter: Adapter[DeveloperState, ApiDeveloperState] = {
    case DeveloperState.Free => Free
    case DeveloperState.Working => Working
    case DeveloperState.Resting => Resting
  }

}