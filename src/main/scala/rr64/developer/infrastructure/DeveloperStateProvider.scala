package rr64.developer.infrastructure

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

trait DeveloperStateProvider {
  def state(implicit ec: ExecutionContext): Future[DeveloperState]
}
