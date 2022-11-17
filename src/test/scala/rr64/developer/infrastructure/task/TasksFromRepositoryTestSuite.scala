package rr64.developer.infrastructure.task

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.{TaskInfo, TaskStatus}

import java.util.UUID
import scala.concurrent.Future

/**
 * Тесты источника задач на основе репозитория
 * */
class TasksFromRepositoryTestSuite extends AsyncFlatSpecLike with Matchers {

  /** Запрос информации о задаче должен делегироваться репозиторию */
  "Single task queries" should "be redirected to the repository" in {
    val task = TaskInfo(UUID.randomUUID(), 33, TaskStatus.Queued)
    val repository = new TaskRepository { // TODO scalamock
      override def save(taskInfo: TaskInfo): Future[_] = ???
      override def findById(id: UUID): Future[Option[TaskInfo]] =
        Future.successful(Some(task))
    }
    val tasks = new TasksFromRepository(repository)
    tasks.findById(task.id).map(_ shouldEqual Some(task))
  }

}
