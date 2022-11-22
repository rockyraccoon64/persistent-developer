package rr64.developer.infrastructure.api

/**
 * Парсер запроса
 * @tparam Input Входные данные
 * @tparam Query Запрос
 * */
trait QueryExtractor[Input, Query] {

  /**
   * Извлечь запрос
   * @param input Входные данные
   * @return Извлечённый запрос или сообщение об ошибке
   * */
  def extract(input: Input): Either[String, Query]

}
