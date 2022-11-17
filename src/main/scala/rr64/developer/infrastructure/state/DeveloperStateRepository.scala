package rr64.developer.infrastructure.state

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

trait DeveloperStateRepository {
  def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[Unit]
  def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]]
}
