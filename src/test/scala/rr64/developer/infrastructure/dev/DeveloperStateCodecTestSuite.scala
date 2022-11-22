package rr64.developer.infrastructure.dev

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.DeveloperState

class DeveloperStateCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new DeveloperStateCodec

  /** Кодек должен кодировать и декодировать состояние разработчика */
  "The developer state codec" should {

    /** "Свободен" */
    "encode and decode the Free state" in {
      val state = DeveloperState.Free
      val encoded = codec.encode(state)
      state shouldEqual codec.decode(encoded)
    }

    /** "Работает" */
    "encode and decode the Working state" in {
      val state = DeveloperState.Working
      val encoded = codec.encode(state)
      state shouldEqual codec.decode(encoded)
    }

  }

}
