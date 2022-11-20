package rr64.developer.infrastructure.api

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ApiError(error: String)

object ApiError {
  implicit val apiErrorJsonFormat: RootJsonFormat[ApiError] =
    jsonFormat1(ApiError.apply)
}