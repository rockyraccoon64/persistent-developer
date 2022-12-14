package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.TaskInfo
import rr64.developer.infrastructure.facade.query.LimitOffsetQueryTestFacade

import scala.concurrent.{ExecutionContext, Future}

/**
 * Тестовый репозиторий задач с использованием Slick
 * */
class TestTaskSlickRepository(repository: TaskSlickRepository)
  extends TestTaskRepository(repository) {

  /** Получить список задач из репозитория на основе Slick */
  def list(limit: Int, offset: Int)
      (implicit ec: ExecutionContext): Future[Seq[TaskInfo]] = {
    val query = LimitOffsetQueryTestFacade.createQuery(limit, offset)
    repository.list(query)
  }

  /** Тестирование запроса списка задач */
  def testListQuery(
    limit: Int,
    offset: Int,
    initial: Seq[TaskInfo],
    expected: Seq[TaskInfo]
  )(implicit ec: ExecutionContext): Future[Assertion] =
    for {
      _ <- saveInSequence(initial)
      list <- list(limit, offset)
    } yield {
      list should contain theSameElementsInOrderAs expected
    }

}