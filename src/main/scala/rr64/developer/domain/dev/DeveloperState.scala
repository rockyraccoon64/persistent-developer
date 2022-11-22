package rr64.developer.domain.dev

/**
 * Состояние разработчика
 * */
sealed trait DeveloperState

object DeveloperState {

  /** Свободен */
  case object Free extends DeveloperState
  /** Работает */
  case object Working extends DeveloperState
  /** Отдыхает */
  case object Resting extends DeveloperState

  /** Начальное состояние */
  val InitialState: DeveloperState = Free

}