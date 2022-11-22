package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState

import scala.concurrent.{ExecutionContext, Future}

/**
 * Репозиторий состояний разработчиков
 * */
trait DeveloperStateRepository {

  /**
   * Сохранить состояние
   * @param id Идентификатор разработчика
   * @param state Состояние разработчика
   * */
  def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[_]

  /**
   * Поиск состояния разработчика по идентификатору
   * @param id Идентификатор разработчика
   * */
  def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]]

}
