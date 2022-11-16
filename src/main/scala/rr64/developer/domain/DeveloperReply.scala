package rr64.developer.domain

import java.util.UUID

sealed trait DeveloperReply

object DeveloperReply {
  case class TaskStarted(id: UUID) extends DeveloperReply
}