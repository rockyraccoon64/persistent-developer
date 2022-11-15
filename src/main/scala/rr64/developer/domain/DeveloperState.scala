package rr64.developer.domain

sealed trait DeveloperState

object DeveloperState {
  case object Free extends DeveloperState
}