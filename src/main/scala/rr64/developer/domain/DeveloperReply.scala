package rr64.developer.domain

sealed trait DeveloperReply

object DeveloperReply {
  case object TaskAccepted extends DeveloperReply
}