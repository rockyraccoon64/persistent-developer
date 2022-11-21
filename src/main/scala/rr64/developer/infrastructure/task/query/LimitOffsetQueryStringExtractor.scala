package rr64.developer.infrastructure.task.query

import rr64.developer.infrastructure.api.QueryExtractor
import rr64.developer.infrastructure.task.query.LimitOffsetQueryStringExtractor._

import scala.util.Try
import scala.util.matching.Regex

class LimitOffsetQueryStringExtractor(
  factory: AbstractLimitOffsetQueryFactory,
  errorMessage: String
) extends QueryExtractor[Option[String], LimitOffsetQuery] {

  override def extract(input: Option[String]): Either[String, LimitOffsetQuery] =
    input match {
      case Some(regex(limitStr, offsetStr)) =>
        Try {
          factory.create(
            limit = limitStr.toInt,
            offset = offsetStr.toInt
          )
        }.toOption.toRight(errorMessage)
      case Some(_) =>
        Left(errorMessage)
      case None =>
        Right(factory.Default)
    }

}

object LimitOffsetQueryStringExtractor {
  private val regex: Regex = """^limit:(\d+),offset:(\d+)$""".r
}