package rr64.developer.infrastructure.task

import rr64.developer.infrastructure.api.QueryExtractor
import rr64.developer.infrastructure.task.LimitOffsetQueryFactory.QueryImpl

import scala.util.Try
import scala.util.matching.Regex

trait LimitOffsetQuery {
  def limit: Int
  def offset: Int
}

object LimitOffsetQuery {

  private val regex: Regex = """^limit:(\d),offset:(\d+)$""".r

  private val errorMessage = "Invalid limit + offset"

  def stringExtractor(factory: LimitOffsetQueryFactory): QueryExtractor[Option[String], LimitOffsetQuery] = {
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

class LimitOffsetException extends RuntimeException

class LimitOffsetQueryFactory(defaultLimit: Int, maxLimit: Int) {
  require(defaultLimit > 0 && maxLimit > 0 && defaultLimit <= maxLimit)

  val Default: LimitOffsetQuery = create()

  def create(
    limit: Int = defaultLimit,
    offset: Int = 0
  ): LimitOffsetQuery = {
    if (limit > 0 && limit <= maxLimit && offset >= 0)
      QueryImpl(limit, offset)
    else
      throw new LimitOffsetException
  }

}

object LimitOffsetQueryFactory {
  private case class QueryImpl(limit: Int, offset: Int) extends LimitOffsetQuery
}