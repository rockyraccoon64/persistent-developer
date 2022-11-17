package rr64.developer.domain

sealed trait DeveloperState

object DeveloperState {
  case object Free extends DeveloperState
  case object Working extends DeveloperState
  case object Resting extends DeveloperState

  val InitialState: DeveloperState = Free
}