package rr64.developer.infrastructure.api

trait Adapter[From, To] {
  def convert(value: From): To
}
