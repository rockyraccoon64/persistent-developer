package rr64.developer.infrastructure.api

import rr64.developer.domain.DeveloperState
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

/**
 * Состояние разработчика для REST API
 * @param state Тип состояния
 * */
case class ApiDeveloperState private(state: String)

object ApiDeveloperState {

  /** Свободен */
  val Free: ApiDeveloperState = ApiDeveloperState("Free")
  /** Работает */
  val Working: ApiDeveloperState = ApiDeveloperState("Working")
  /** Отдыхает */
  val Resting: ApiDeveloperState = ApiDeveloperState("Resting")

  /** JSON-формат состояния разработчика */
  implicit val developerStateJsonFormat: RootJsonFormat[ApiDeveloperState] =
    jsonFormat1(ApiDeveloperState.apply)

  /** Адаптер доменного состояния разработчика к используемому в REST API */
  implicit val adapter: Adapter[DeveloperState, ApiDeveloperState] = {
    case DeveloperState.Free => Free
    case DeveloperState.Working => Working
    case DeveloperState.Resting => Resting
  }

}