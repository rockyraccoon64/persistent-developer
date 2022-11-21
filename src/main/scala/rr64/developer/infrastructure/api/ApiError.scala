package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiError(error: String, message: String)

object ApiError {
  implicit val apiErrorJsonFormat: RootJsonFormat[ApiError] =
    jsonFormat2(ApiError.apply)

  def inQuery(message: String): ApiError =
    ApiError("Query", message)

  val TaskDifficulty: ApiError =
    ApiError("DifficultyRange", "Difficulty should be in the range [1-100]")
}