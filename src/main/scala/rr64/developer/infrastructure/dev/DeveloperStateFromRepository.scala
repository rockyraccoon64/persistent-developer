package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

/**
 * Источник состояния разработчика из репозитория
 * */
class DeveloperStateFromRepository(
  developerId: String,
  repository: DeveloperStateRepository
) extends DeveloperStateProvider {
  /** Состояние разработчика */
  override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
    repository.findById(developerId).map(_.getOrElse(DeveloperState.InitialState))
}
