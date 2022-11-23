package rr64.developer.infrastructure.api.model

import rr64.developer.domain.task.{Difficulty, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.api.Adapter
import rr64.developer.infrastructure.api.CommonJsonFormats._
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.util.UUID

/**
 * Представление существующей задачи для REST API
 * @param id Идентификатор
 * @param difficulty Сложность
 * @param status Текущий статус
 * */
case class ApiTaskInfo(id: UUID, difficulty: Int, status: String)

object ApiTaskInfo {

  /** JSON-формат существующей задачи для REST API */
  implicit val apiTaskInfoJsonFormat: RootJsonFormat[ApiTaskInfo] =
    jsonFormat3(ApiTaskInfo.apply)

  /** Адаптер доменной задачи к представлению для REST API */
  implicit val adapter: Adapter[TaskInfo, ApiTaskInfo] = value => {
    val TaskInfo(
      id: UUID,
      difficulty: Difficulty,
      status: TaskStatus
    ) = value

    val statusString = status match {
      case TaskStatus.InProgress => "InProgress"
      case TaskStatus.Queued => "Queued"
      case TaskStatus.Finished => "Finished"
    }

    ApiTaskInfo(id, difficulty.value, statusString)
  }
}