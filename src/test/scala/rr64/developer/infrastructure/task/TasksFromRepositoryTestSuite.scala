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
      override def list: Future[Seq[TaskInfo]] = ???
    }
    val tasks = new TasksFromRepository(repository)
    tasks.findById(task.id).map(_ shouldEqual Some(task))
  }

  /** Запрос списка задач должен делегироваться репозиторию */
  "Task list queries" should "be delegated to the repository" in {
    val task1 = TaskInfo(UUID.randomUUID(), 33, TaskStatus.Queued)
    val task2 = TaskInfo(UUID.randomUUID(), 85, TaskStatus.InProgress)
    val repository = new TaskRepository { // TODO scalamock
      override def save(taskInfo: TaskInfo): Future[_] = Future.unit
      override def findById(id: UUID): Future[Option[TaskInfo]] =
        Future.successful(Some(task1))
      override def list: Future[Seq[TaskInfo]] =
        Future.successful(Seq(task1, task2))
    }
    val tasks = new TasksFromRepository(repository)
    tasks.list.map(_ shouldEqual Seq(task1, task2))
  }

}
