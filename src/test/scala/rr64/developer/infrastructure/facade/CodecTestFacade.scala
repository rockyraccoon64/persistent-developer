package rr64.developer.infrastructure.facade

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import rr64.developer.infrastructure.Codec

/**
 * Фасад для тестов с использованием кодеков
 */
trait CodecTestFacade {

  /** Проверка симметричности кодирования и декодирования */
  def assertCodecSymmetrical[A, B](codec: Codec[A, B])(value: A) {
    val encoded = codec.encode(value)
    codec.decode(encoded) shouldEqual value
  }

}

object CodecTestFacade
  extends CodecTestFacade