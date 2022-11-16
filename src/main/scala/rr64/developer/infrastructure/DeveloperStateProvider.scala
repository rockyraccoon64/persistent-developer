package rr64.developer.infrastructure

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

/**
 * Источник состояния разработчика
 */
trait DeveloperStateProvider {
  /** Состояние разработчика */
  def state(implicit ec: ExecutionContext): Future[DeveloperState]
}
