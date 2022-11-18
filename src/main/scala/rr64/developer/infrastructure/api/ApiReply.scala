package rr64.developer.infrastructure.api

import rr64.developer.infrastructure.api.CommonJsonFormats._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.util.UUID

case class ApiReply(
  id: UUID,
  status: String
)

object ApiReply {
  implicit val apiReplyJsonFormat: RootJsonFormat[ApiReply] = jsonFormat2(ApiReply.apply)
}