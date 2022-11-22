package rr64.developer.domain.task

/**
 * Статус задачи
 * */
sealed trait TaskStatus

object TaskStatus {
  /** В работе */
  case object InProgress extends TaskStatus
  /** В очереди */
  case object Queued extends TaskStatus
  /** Завершена */
  case object Finished extends TaskStatus
}
