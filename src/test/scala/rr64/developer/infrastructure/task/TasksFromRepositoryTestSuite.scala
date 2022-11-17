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

  /** Запрос информации о задаче должен делегироваться репозиторию */
  "Single task queries" should "be redirected to the repository" in {
    val task = TaskInfo(UUID.randomUUID(), 33, TaskStatus.Queued)
    val mockRepo = mock[TaskRepository]
    (mockRepo.findById _)
      .expects(task.id)
      .once()
      .returning {
        Future.successful(Some(task))
      }
    val tasks = new TasksFromRepository(mockRepo)
    tasks.findById(task.id).map(_ shouldEqual Some(task))
  }

  /** Запрос списка задач должен делегироваться репозиторию */
  "Task list queries" should "be delegated to the repository" in {
    val task1 = TaskInfo(UUID.randomUUID(), 33, TaskStatus.Queued)
    val task2 = TaskInfo(UUID.randomUUID(), 85, TaskStatus.InProgress)
    val mockRepo = mock[TaskRepository]
    (mockRepo.list _)
      .expects()
      .once()
      .returning {
        Future.successful(Seq(task1, task2))
      }
    val tasks = new TasksFromRepository(mockRepo)
    tasks.list.map(_ shouldEqual Seq(task1, task2))
  }

}
