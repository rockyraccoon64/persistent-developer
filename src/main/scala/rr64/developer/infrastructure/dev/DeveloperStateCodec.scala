package rr64.developer.infrastructure.dev

import rr64.developer.domain.DeveloperState
import rr64.developer.infrastructure.Codec

class DeveloperStateCodec extends Codec[DeveloperState, String] {
  override def encode(value: DeveloperState): String = ???
  override def decode(value: String): DeveloperState = ???
}
