package rr64.developer.infrastructure.api

import rr64.developer.domain.DeveloperReply
import rr64.developer.infrastructure.api.CommonJsonFormats._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.util.UUID

/**
 * Представление ответа разработчика для REST API
 * */
case class ApiReply(id: UUID, status: String)

object ApiReply {

  /** JSON-формат ответа разработчика */
  implicit val apiReplyJsonFormat: RootJsonFormat[ApiReply] = jsonFormat2(ApiReply.apply)

  /** Адаптер доменного ответа разработчика к представлению для REST API */
  implicit val adapter: Adapter[DeveloperReply, ApiReply] = {
    case DeveloperReply.TaskStarted(id) => ApiReply(id, "Started")
    case DeveloperReply.TaskQueued(id) => ApiReply(id, "Queued")
  }

}