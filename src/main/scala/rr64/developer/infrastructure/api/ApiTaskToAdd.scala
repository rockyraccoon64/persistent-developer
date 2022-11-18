package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiTaskToAdd(difficulty: Int)

object ApiTaskToAdd {
  implicit val apiAddTaskJsonFormat: RootJsonFormat[ApiTaskToAdd] =
    jsonFormat1(ApiTaskToAdd.apply)
}
