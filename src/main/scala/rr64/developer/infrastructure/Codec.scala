package rr64.developer.infrastructure

trait Codec[A, B] {
  def encode(value: A): B
  def decode(value: B): A
}
