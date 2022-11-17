package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.infrastructure.PostgresSpec

class TaskSlickRepositoryTestSuite
  extends PostgresSpec
    with AsyncFlatSpecLike
    with Matchers {

  /** Репозиторий должен сохранять задачи со статусом "В очереди" */

  /** Репозиторий должен сохранять задачи со статусом "В разработке" */

  /** Репозиторий должен сохранять задачи со статусом "Завершено" */

  /** Репозиторий должен обновлять статус у существующих задач */

  /** Репозиторий должен находить существующие задачи */

  /** Репозиторий должен не должен находить несуществующие задачи */

  /** Репозиторий должен возвращать список всех задач */

}
