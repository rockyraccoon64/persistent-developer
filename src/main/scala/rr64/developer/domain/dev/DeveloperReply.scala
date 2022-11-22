package rr64.developer.domain.dev

import java.util.UUID

/**
 * Ответ разработчика на команду
 */
sealed trait DeveloperReply

object DeveloperReply {
  /**
   * Задача принята в разработку
   * @param id Идентификатор задачи
   * */
  case class TaskStarted(id: UUID) extends DeveloperReply

  /**
   * Задача поставлена в очередь
   * @param id Идентификатор задачи
   * */
  case class TaskQueued(id: UUID) extends DeveloperReply
}