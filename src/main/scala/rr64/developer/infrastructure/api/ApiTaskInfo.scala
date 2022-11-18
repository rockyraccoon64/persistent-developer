package rr64.developer.infrastructure.api

import rr64.developer.infrastructure.api.CommonJsonFormats._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.util.UUID

case class ApiTaskInfo(id: UUID, difficulty: Int, status: String)

object ApiTaskInfo {
  implicit val apiTaskInfoJsonFormat: RootJsonFormat[ApiTaskInfo] =
    jsonFormat3(ApiTaskInfo.apply)
}