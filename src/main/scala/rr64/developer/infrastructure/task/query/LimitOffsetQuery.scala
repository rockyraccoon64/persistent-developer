package rr64.developer.infrastructure.task.query

import rr64.developer.infrastructure.api.QueryExtractor

import scala.util.Try
import scala.util.matching.Regex

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

class LimitOffsetQueryStringExtractor(factory: LimitOffsetQueryFactory)
  extends QueryExtractor[Option[String], LimitOffsetQuery] {

  import LimitOffsetQueryStringExtractor._

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
  private val regex: Regex = """^limit:(\d),offset:(\d+)$""".r
  private val errorMessage = "Invalid limit + offset"
}