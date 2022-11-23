package rr64.developer.infrastructure.api.model

import rr64.developer.domain.task.Task
import rr64.developer.infrastructure.api.Adapter
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

/**
 * Представление новой задачи для REST API
 * @param difficulty Сложность задачи
 * */
case class ApiTaskToAdd(difficulty: Int)

object ApiTaskToAdd {

  /** JSON-формат новой задачи */
  implicit val apiAddTaskJsonFormat: RootJsonFormat[ApiTaskToAdd] =
    jsonFormat1(ApiTaskToAdd.apply)

  /** Адаптер новой задачи к доменному представлению */
  implicit val adapter: Adapter[ApiTaskToAdd, Task] =
    apiTask => Task(apiTask.difficulty)

}
