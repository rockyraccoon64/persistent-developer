package rr64.developer.infrastructure

/**
 * Кодек (двустороннее преобразование)
 * */
trait Codec[A, B] {

  /** Кодировать значение */
  def encode(value: A): B

  /** Декодировать значение */
  def decode(value: B): A

}
