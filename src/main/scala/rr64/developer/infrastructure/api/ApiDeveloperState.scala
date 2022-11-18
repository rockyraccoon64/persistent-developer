package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiDeveloperState private(state: String)

object ApiDeveloperState {

  val Working: ApiDeveloperState = ApiDeveloperState("Working")

  implicit val developerStateJsonFormat: RootJsonFormat[ApiDeveloperState] =
    jsonFormat1(ApiDeveloperState.apply)

}