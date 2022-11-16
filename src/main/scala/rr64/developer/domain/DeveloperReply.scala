package rr64.developer.domain

import java.util.UUID

sealed trait DeveloperReply

object DeveloperReply {
  case class TaskAccepted(id: UUID) extends DeveloperReply
}