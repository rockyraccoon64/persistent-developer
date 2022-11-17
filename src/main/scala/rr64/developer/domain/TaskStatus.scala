package rr64.developer.domain

sealed trait TaskStatus

object TaskStatus {
  case object InProgress extends TaskStatus
  case object Queued extends TaskStatus
  case object Finished extends TaskStatus
}
