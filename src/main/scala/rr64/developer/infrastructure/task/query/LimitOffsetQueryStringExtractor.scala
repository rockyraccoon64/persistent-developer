package rr64.developer.infrastructure.task.query

import rr64.developer.infrastructure.api.QueryExtractor
import rr64.developer.infrastructure.task.query.LimitOffsetQueryStringExtractor._

import scala.util.Try
import scala.util.matching.Regex

/**
 * Парсер параметров запроса с использованием limit/offset из строки
 * Например: limit:5,offset:15
 * @param factory Фабрика параметров запроса
 * @param errorMessage Сообщение об ошибке из-за некорректного формата строки
 */
class LimitOffsetQueryStringExtractor(
  factory: LimitOffsetQueryFactory,
  errorMessage: String
) extends QueryExtractor[Option[String], LimitOffsetQuery] {

  override def extract(input: Option[String]): Either[String, LimitOffsetQuery] =
    input match {
      case Some(queryRegex(limitStr, offsetStr)) =>
        Try {
          factory.create(
            limit = limitStr.toInt,
            offset = offsetStr.toInt
          )
        }.toOption.toRight(errorMessage)
      case Some(_) =>
        Left(errorMessage)
      case None =>
        Right(factory.default)
    }

}

object LimitOffsetQueryStringExtractor {
  /** Регулярное выражение параметров запроса */
  private val queryRegex: Regex = """^limit:(\d+),offset:(\d+)$""".r
}