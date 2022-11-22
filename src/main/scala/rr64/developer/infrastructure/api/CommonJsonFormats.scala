package rr64.developer.infrastructure.api

import spray.json.{JsString, JsValue, JsonFormat, deserializationError}

import java.util.UUID

/**
 * Общие JSON-форматы
 * */
trait CommonJsonFormats {

  /** JSON-формат UUID */
  implicit val uuidJsonFormat: JsonFormat[UUID] = new JsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)
    override def read(json: JsValue): UUID = json match {
      case JsString(str) => UUID.fromString(str)
      case _ => deserializationError("Invalid UUID format")
    }
  }

}

object CommonJsonFormats extends CommonJsonFormats
