package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

/**
 * Представление ошибки для REST API
 * */
case class ApiError(error: String, message: String)

object ApiError {

  /** Ошибка в запросе */
  def inQuery(message: String): ApiError =
    ApiError("Query", message)

  /** Ошибка, связанная со сложностью задачи */
  val TaskDifficulty: ApiError =
    ApiError("DifficultyRange", "Difficulty should be in the range [1-100]")

  /** JSON-формат ошибки */
  implicit val apiErrorJsonFormat: RootJsonFormat[ApiError] =
    jsonFormat2(ApiError.apply)

}