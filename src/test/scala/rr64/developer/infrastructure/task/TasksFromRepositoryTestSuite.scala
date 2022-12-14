package rr64.developer.infrastructure.task

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.infrastructure.facade.task.TaskTestFacade._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты источника задач на основе репозитория
 * */
class TasksFromRepositoryTestSuite
  extends AsyncMockFactory
    with AsyncFlatSpecLike
    with Matchers {

  private val task1 = createTaskInfo(
    id = "a68b3067-7ce1-4da2-9e2a-b8b38e9863d1",
    difficulty = 33,
    status = queuedTaskStatus)
  private val task2 = createTaskInfo(
    id = "011a141e-d0a5-4693-9cd1-75a85c6284e1",
    difficulty = 85,
    status = inProgressTaskStatus)

  private val mockRepository = mock[TaskRepository[Any]]
  private val tasks = new TasksFromRepository(mockRepository)

  /** Запрос информации о задаче должен делегироваться репозиторию */
  "Single task queries" should "be redirected to the repository" in {
    val task = task1
    (mockRepository.findById(_: UUID)(_: ExecutionContext))
      .expects(task.id, *)
      .returning {
        Future.successful(Some(task))
      }
    tasks.findById(task.id).map(_ shouldEqual Some(task))
  }

  /** Запрос списка задач должен делегироваться репозиторию */
  "Task list queries" should "be delegated to the repository" in {
    val taskSeq = Seq(task1, task2)
    (mockRepository.list(_: Any)(_: ExecutionContext))
      .expects(*, *)
      .returning {
        Future.successful(taskSeq)
      }
    tasks.list().map(_ should contain theSameElementsInOrderAs taskSeq)
  }

}
