package rr64.developer.infrastructure.dev

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.DeveloperState

class DeveloperStateCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new DeveloperStateCodec

  private def testCodec(state: DeveloperState) = {
    val encoded = codec.encode(state)
    state shouldEqual codec.decode(encoded)
  }

  /** Кодек должен кодировать и декодировать состояние разработчика */
  "The developer state codec" should {

    /** "Свободен" */
    "encode and decode the Free state" in testCodec(DeveloperState.Free)

    /** "Работает" */
    "encode and decode the Working state" in testCodec(DeveloperState.Working)

    /** "Отдыхает" */
    "encode and decode the Resting state" in testCodec(DeveloperState.Resting)

  }

}
