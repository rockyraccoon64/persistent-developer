package rr64.developer.infrastructure.api

import rr64.developer.domain.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.api.CommonJsonFormats._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.util.UUID

case class ApiTaskInfo(id: UUID, difficulty: Int, status: String)

object ApiTaskInfo {
  implicit val apiTaskInfoJsonFormat: RootJsonFormat[ApiTaskInfo] =
    jsonFormat3(ApiTaskInfo.apply)

  implicit val adapter: Adapter[TaskInfo, ApiTaskInfo] = value => {
    val TaskInfo(
      id: UUID,
      difficulty: Int,
      status: TaskStatus
    ) = value

    val statusString = status match {
      case TaskStatus.InProgress => "InProgress"
      case TaskStatus.Queued => "Queued"
      case TaskStatus.Finished => "Finished"
    }

    ApiTaskInfo(id, difficulty, statusString)
  }
}