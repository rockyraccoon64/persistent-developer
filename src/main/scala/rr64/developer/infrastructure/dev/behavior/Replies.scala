package rr64.developer.infrastructure.dev.behavior

import java.util.UUID

/**
 * Ответы актора разработчика
 * */
object Replies {

  /** Результат добавления задачи */
  sealed trait AddTaskResult

  /**
   * Задача принята в работу
   * @param id Идентификатор задачи
   * */
  case class TaskStarted(id: UUID) extends AddTaskResult

  /**
   * Задача поставлена в очередь
   * @param id Идентификатор задачи
   * */
  case class TaskQueued(id: UUID) extends AddTaskResult

}
