package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

trait DeveloperStateRepository {
  def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[_]
  def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]]
}