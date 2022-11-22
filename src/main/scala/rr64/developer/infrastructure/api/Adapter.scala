package rr64.developer.infrastructure.api

/**
 * Адаптер одного типа к другому
 * */
trait Adapter[From, To] {

  /** Преобразовать значение */
  def convert(value: From): To

}
