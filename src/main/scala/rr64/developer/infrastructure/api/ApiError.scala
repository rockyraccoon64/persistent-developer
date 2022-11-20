package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiError(error: String, message: String)

object ApiError {
  implicit val apiErrorJsonFormat: RootJsonFormat[ApiError] =
    jsonFormat2(ApiError.apply)

  val TaskDifficulty: ApiError =
    ApiError("TaskDifficulty", "Tasks should have difficulty [1-100]")
}