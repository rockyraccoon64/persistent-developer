package rr64.developer.infrastructure.task

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._
import rr64.developer.domain.task.TaskInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}


/**
 * Тестовый репозиторий задач
 * */
class TestTaskRepository[Q](repository: TaskRepository[Q]) {

  /** Сохранить задачу в репозитории */
  def save(task: TaskInfo): Future[_] =
    repository.save(task)

  /** Сохранить последовательность задач в репозитории */
  def saveInSequence(tasks: Seq[TaskInfo])
      (implicit ec: ExecutionContext): Future[Any] =
    tasks.foldLeft[Future[Any]](Future.unit) { (acc, task) =>
      acc.flatMap(_ => save(task))
    }

  /** Найти задачу в репозитории */
  def find(id: UUID)
      (implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
    repository.findById(id)

  /** Проверить, что задача существует в репозитории */
  def assertExists(task: TaskInfo)
      (implicit ec: ExecutionContext): Future[Assertion] =
    for (taskOpt <- find(task.id)) yield
      taskOpt shouldEqual Some(task)

  /** Сохранить задачу и проверить, что она сохранена */
  def saveAndAssert(task: TaskInfo)
      (implicit ec: ExecutionContext): Future[Assertion] =
    for {
      _ <- save(task)
      succeeded <- assertExists(task)
    } yield succeeded

}