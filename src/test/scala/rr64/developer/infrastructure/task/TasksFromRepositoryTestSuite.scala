package rr64.developer.infrastructure.task

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.{TaskInfo, TaskStatus}

import java.util.UUID
import scala.concurrent.Future

/**
 * Тесты источника задач на основе репозитория
 * */
class TasksFromRepositoryTestSuite
  extends AsyncMockFactory
    with AsyncFlatSpecLike
    with Matchers {

  private val task1 = TaskInfo(UUID.randomUUID(), 33, TaskStatus.Queued)
  private val task2 = TaskInfo(UUID.randomUUID(), 85, TaskStatus.InProgress)

  /** Запрос информации о задаче должен делегироваться репозиторию */
  "Single task queries" should "be redirected to the repository" in {
    val task = task1
    val mockRepository = mock[TaskRepository]
    (mockRepository.findById _)
      .expects(task.id)
      .once()
      .returning {
        Future.successful(Some(task))
      }
    val tasks = new TasksFromRepository(mockRepository)
    tasks.findById(task.id).map(_ shouldEqual Some(task))
  }

  /** Запрос списка задач должен делегироваться репозиторию */
  "Task list queries" should "be delegated to the repository" in {
    val taskSeq = Seq(task1, task2)
    val mockRepository = mock[TaskRepository]
    (mockRepository.list _)
      .expects()
      .once()
      .returning {
        Future.successful(taskSeq)
      }
    val tasks = new TasksFromRepository(mockRepository)
    tasks.list.map(_ should contain theSameElementsInOrderAs taskSeq)
  }

}