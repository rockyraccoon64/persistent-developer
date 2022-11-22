package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.Codec
import rr64.developer.infrastructure.dev.DeveloperStateCodec._

/**
 * Кодек состояний разработчика
 */
class DeveloperStateCodec extends Codec[DeveloperState, String] {
  override def encode(value: DeveloperState): String =
    value match {
      case DeveloperState.Free => FreeState
      case DeveloperState.Working => WorkingState
      case DeveloperState.Resting => RestingState
    }
  override def decode(value: String): DeveloperState =
    value match {
      case FreeState => DeveloperState.Free
      case WorkingState => DeveloperState.Working
      case RestingState => DeveloperState.Resting
    }
}

object DeveloperStateCodec {
  private val FreeState = "Free"
  private val WorkingState = "Working"
  private val RestingState = "Resting"
}