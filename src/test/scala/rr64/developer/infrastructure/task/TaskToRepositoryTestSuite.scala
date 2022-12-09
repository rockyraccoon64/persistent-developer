package rr64.developer.infrastructure.task

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.{ProjectionTestKit, TestProjection}
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.task.TaskInfo.TaskInfoFromTaskWithId
import rr64.developer.domain.task.{TaskInfo, TaskStatus}
import rr64.developer.infrastructure.EventProjectionTestFacade._
import rr64.developer.infrastructure.EventTestFacade._
import rr64.developer.infrastructure.dev.behavior.Event
import rr64.developer.infrastructure.task.TaskTestFacade._

import scala.concurrent.ExecutionContext

/**
 * Тесты обработчика проекции задач
 */
class TaskToRepositoryTestSuite
  extends ScalaTestWithActorTestKit
    with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)
  private implicit val ec: ExecutionContext = system.executionContext

  /** Фикстура для тестирования обработчика проекции */
  private trait HandlerTest {

    /** Репозиторий, изначально пустой */
    protected val mockRepository: TaskRepository[Any] =
      simpleTaskRepository

    /** Обработчик проекции */
    protected val handler: Handler[EventEnvelope[Event]] =
      new TaskToRepository(mockRepository)

    /** Идентификатор проекции */
    private val projectionId = ProjectionId("task-proj-test", "0")

    /** Persistence ID актора, от которого пришли события */
    private val persistenceId = "test-id"

    /** Проекция на основе последовательности событий */
    protected def projectionFromEvents(
      events: Seq[Event]
    ): TestProjection[Offset, EventEnvelope[Event]] =
      projectionFromEventSequence(
        handler,
        projectionId
      )(
        events,
        persistenceId
      )

    /** Проверка состояния задачи */
    protected def assertInfo(taskInfo: TaskInfo): Assertion =
      assertTaskExistsInRepository(mockRepository)(taskInfo).futureValue

    protected def projection: TestProjection[Offset, EventEnvelope[Event]]

    protected def assertAllSaved(tasks: TaskInfo*): Unit =
      projectionTestKit.run(projection) {
        tasks.foreach(assertInfo)
      }

  }

  /** В начале работы над задачей информация о текущем статусе должна сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started" in
    new HandlerTest {
      val taskWithId = createTaskWithId(90, "d4e174a6-eed3-4fc6-8708-1f2a290cec0c")
      val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
      val events = taskStartedEvent(taskWithId) :: Nil
      val projection = projectionFromEvents(events)
      assertAllSaved(taskInfo)
    }

  /** Когда задача ставится в очередь, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is queued" in
    new HandlerTest {
      val taskWithId = createTaskWithId(100, "6a03f38c-72c8-4a2d-be6f-d0b16c88fcae")
      val taskInfo = taskWithId.withStatus(TaskStatus.Queued)
      val events = taskQueuedEvent(taskWithId) :: Nil
      val projection = projectionFromEvents(events)
      assertAllSaved(taskInfo)
    }

  /** Когда задача завершена, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is finished" in
    new HandlerTest {
      val taskWithId = createTaskWithId(77, "ee53c62a-9b14-4969-ba3e-620fb42f30bc")
      val taskInfo = taskWithId.withStatus(TaskStatus.Finished)
      val events = taskFinishedEvent(taskWithId) :: Nil
      val projection = projectionFromEvents(events)
      assertAllSaved(taskInfo)
    }

  /** В начале работы над задачей после отдыха её статус должен сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started after resting" in
    new HandlerTest {
      val taskWithId = createTaskWithId(35, "f33b67f0-2324-4c7d-8b5f-59ab8e4f5bd7")
      val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
      val events = Event.Rested(Some(taskWithId)) :: Nil
      val projection = projectionFromEvents(events)
      assertAllSaved(taskInfo)
    }

  /** Когда событие не связано с задачей, обновления не происходит */
  "The task state" should "not be updated when there's no task events" in
    new HandlerTest {
      val taskWithId1 = createTaskWithId(53, "ed03da50-6836-4f01-9c46-47775b419c3d")
      val taskWithId2 = createTaskWithId(10, "3ff6c9a5-8fd8-4e3a-840b-a3823e33fffa")
      val taskInfo1 = taskWithId1.withStatus(TaskStatus.Queued)
      val taskInfo2 = taskWithId2.withStatus(TaskStatus.Finished)
      val events = taskQueuedEvent(taskWithId1) ::
        taskFinishedEvent(taskWithId2) ::
        Event.Rested(None) ::
        Nil
      val projection = projectionFromEvents(events)
      assertAllSaved(taskInfo1, taskInfo2)
    }

}
