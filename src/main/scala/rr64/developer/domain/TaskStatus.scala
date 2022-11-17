package rr64.developer.domain

sealed trait TaskStatus

object TaskStatus {
  case object Queued extends TaskStatus
}
