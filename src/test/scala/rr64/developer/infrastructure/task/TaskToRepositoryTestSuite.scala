package rr64.developer.infrastructure.task

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.projection.ProjectionId
import akka.projection.testkit.scaladsl.ProjectionTestKit
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.infrastructure.DeveloperEventProjectionTestFacade._
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

    /** События, на основе которых строится проекция */
    protected def events: Seq[Event]

    /** Запуск проекции и проверка задач в репозитории */
    protected def assertAllSaved(tasks: TaskInfo*): Unit =
      projectionTestKit.run(projection) {
        tasks.foreach(assertInfo)
      }

    /** Проекция на основе последовательности событий */
    private lazy val projection: TestProj =
      projectionFromEventSequence(
        handler,
        projectionId
      )(
        events,
        persistenceId
      )

    /** Репозиторий, изначально пустой */
    private val mockRepository: TaskRepository[Any] =
      simpleTaskRepository

    /** Обработчик проекции */
    private val handler: ProjHandler =
      new TaskToRepository(mockRepository)

    /** Идентификатор проекции */
    private val projectionId =
      ProjectionId("task-proj-test", "0")

    /** Persistence ID актора, от которого приходят события */
    private val persistenceId = "test-id"

    /** Проверка состояния задачи */
    private def assertInfo(taskInfo: TaskInfo): Assertion =
      assertTaskExistsInRepository(mockRepository)(taskInfo).futureValue

  }

  /** В начале работы над задачей информация о текущем статусе должна сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started" in
    new HandlerTest {
      val taskWithId = createTaskWithId(90, "d4e174a6-eed3-4fc6-8708-1f2a290cec0c")
      val taskInfo = taskWithId.withStatus(inProgressTaskStatus)
      val events = taskStartedEvent(taskWithId) :: Nil
      assertAllSaved(taskInfo)
    }

  /** Когда задача ставится в очередь, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is queued" in
    new HandlerTest {
      val taskWithId = createTaskWithId(100, "6a03f38c-72c8-4a2d-be6f-d0b16c88fcae")
      val taskInfo = taskWithId.withStatus(queuedTaskStatus)
      val events = taskQueuedEvent(taskWithId) :: Nil
      assertAllSaved(taskInfo)
    }

  /** Когда задача завершена, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is finished" in
    new HandlerTest {
      val taskWithId = createTaskWithId(77, "ee53c62a-9b14-4969-ba3e-620fb42f30bc")
      val taskInfo = taskWithId.withStatus(finishedTaskStatus)
      val events = taskFinishedEvent(taskWithId) :: Nil
      assertAllSaved(taskInfo)
    }

  /** В начале работы над задачей после отдыха её статус должен сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started after resting" in
    new HandlerTest {
      val taskWithId = createTaskWithId(35, "f33b67f0-2324-4c7d-8b5f-59ab8e4f5bd7")
      val taskInfo = taskWithId.withStatus(inProgressTaskStatus)
      val events = restedEvent(Some(taskWithId)) :: Nil
      assertAllSaved(taskInfo)
    }

  /** Когда событие не связано с задачей, обновления не происходит */
  "The task state" should "not be updated when there's no task events" in
    new HandlerTest {
      val taskWithId1 = createTaskWithId(53, "ed03da50-6836-4f01-9c46-47775b419c3d")
      val taskWithId2 = createTaskWithId(10, "3ff6c9a5-8fd8-4e3a-840b-a3823e33fffa")
      val taskInfo1 = taskWithId1.withStatus(queuedTaskStatus)
      val taskInfo2 = taskWithId2.withStatus(finishedTaskStatus)
      val events = taskQueuedEvent(taskWithId1) ::
        taskFinishedEvent(taskWithId2) ::
        restedEvent(None) ::
        Nil
      assertAllSaved(taskInfo1, taskInfo2)
    }

}
