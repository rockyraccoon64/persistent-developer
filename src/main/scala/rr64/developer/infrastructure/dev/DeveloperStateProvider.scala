package rr64.developer.infrastructure.dev

import rr64.developer.domain.dev.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

/**
 * Источник состояния разработчика
 */
trait DeveloperStateProvider {
  /** Состояние разработчика */
  def state(implicit ec: ExecutionContext): Future[DeveloperState]
}
