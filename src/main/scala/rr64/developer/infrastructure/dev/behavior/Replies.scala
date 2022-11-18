package rr64.developer.infrastructure.dev.behavior

import java.util.UUID

/**
 * Ответы актора разработчика
 * */
object Replies {
  /** Результат добавления задачи */
  sealed trait AddTaskResult
  /** Задача принята в работу */
  case class TaskStarted(id: UUID) extends AddTaskResult
  /** Задача поставлена в очередь */
  case class TaskQueued(id: UUID) extends AddTaskResult
}
