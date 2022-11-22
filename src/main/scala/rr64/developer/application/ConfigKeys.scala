package rr64.developer.application

/**
 * Ключи файла конфигурации
 */
object ConfigKeys {

  /** Конфигурация приложения */
  val AppConfig = "persistent-dev"

  /** Максимальное время ожидания запросов акторам */
  val AskTimeout = "ask-timeout"

  /** Имя корневого актора */
  val RootGuardianName = "root-guardian-name"

  /** Имя актора разработчика */
  val DeveloperActorName = "developer.name"

  /** Persistence ID актора разработчика */
  val DeveloperPersistenceId = "developer.persistence-id"

  /** Рабочий множитель */
  val WorkFactor = "developer.work-factor"

  /** Множитель отдыха */
  val RestFactor = "developer.rest-factor"

  /** Длина списка задач в запросе по умолчанию */
  val DefaultLimit = "tasks.default-limit"

  /** Максимальная длина списка задач в запросе */
  val MaxLimit = "tasks.max-limit"

  /** Интерфейс REST API */
  val ApiInterface = "api.interface"

  /** Порт REST API */
  val ApiPort = "api.port"

}
