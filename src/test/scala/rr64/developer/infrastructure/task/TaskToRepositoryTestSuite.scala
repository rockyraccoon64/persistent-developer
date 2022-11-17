package rr64.developer.infrastructure.task

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.{ProjectionTestKit, TestProjection}
import akka.stream.scaladsl.Source
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.{Task, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.ProjectionTestUtils
import rr64.developer.infrastructure.TaskTestUtils.TaskWithIdFactory
import rr64.developer.infrastructure.dev.DeveloperBehavior.Event
import rr64.developer.infrastructure.task.TaskToRepository.TaskInfoFactory

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты обработчика проекции задач
 */
class TaskToRepositoryTestSuite
  extends ScalaTestWithActorTestKit
    with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)
  private implicit val ec: ExecutionContext = system.executionContext

  trait Fixture {

    protected val mockRepository: TaskRepository = new TaskRepository {
      private var tasks: Map[UUID, TaskInfo] = Map.empty
      override def save(taskInfo: TaskInfo): Future[_] = {
        tasks = tasks.updated(taskInfo.id, taskInfo)
        Future.unit
      }
      override def findById(id: UUID)(implicit ec: ExecutionContext): Future[Option[TaskInfo]] =
        Future.successful(tasks.get(id))
      override def list(implicit ec: ExecutionContext): Future[Seq[TaskInfo]] =
        Future.successful(tasks.values.toSeq)
    }

    protected val handler: Handler[EventEnvelope[Event]] = new TaskToRepository(mockRepository)

    protected def projectionFromEvents(
      events: Seq[Event],
      persistenceId: String = "proj"
    ): TestProjection[Offset, EventEnvelope[Event]] = {
      val source = ProjectionTestUtils.envelopeSource(events, persistenceId)
      projectionFromSource(source)
    }

    protected def projectionFromSource(
      source: Source[EventEnvelope[Event], NotUsed]
    ): TestProjection[Offset, EventEnvelope[Event]] =
      TestProjection(
        projectionId = ProjectionId("task-proj-test", "0"),
        sourceProvider = ProjectionTestUtils.providerFromSource(source),
        handler = () => handler
      )

    protected def assertInfo(taskInfo: TaskInfo): Assertion =
      mockRepository.findById(taskInfo.id).futureValue shouldEqual Some(taskInfo)

  }

  /** В начале работы над задачей информация о текущем статусе должна сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started" in new Fixture {
    val taskWithId = Task(90).withRandomId
    val taskInfo = taskWithId.withStatus(TaskStatus.InProgress)
    val events = Event.TaskStarted(taskWithId) :: Nil
    val projection = projectionFromEvents(events)
    projectionTestKit.run(projection) {
      assertInfo(taskInfo)
    }
  }

  /** Когда задача ставится в очередь, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is queued" in new Fixture {
    val taskWithId = Task(100).withRandomId
    val taskInfo = taskWithId.withStatus(TaskStatus.Queued)
    val events = Event.TaskQueued(taskWithId) :: Nil
    val projection = projectionFromEvents(events)
    projectionTestKit.run(projection) {
      assertInfo(taskInfo)
    }
  }

  /** Когда задача завершена, её текущее состояние должно сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is finished" in new Fixture {
    val taskWithId = Task(77).withRandomId
    val taskInfo = taskWithId.withStatus(TaskStatus.Finished)
    val events = Event.TaskFinished(taskWithId) :: Nil
    val projection = projectionFromEvents(events)
    projectionTestKit.run(projection) {
      assertInfo(taskInfo)
    }
  }

  /** Когда событие не связано с задачей, обновления не происходит */
  "The task state" should "not be updated when there's no task events" in new Fixture {
    val taskWithId1 = Task(53).withRandomId
    val taskWithId2 = Task(10).withRandomId
    val taskInfo1 = taskWithId1.withStatus(TaskStatus.Queued)
    val taskInfo2 = taskWithId2.withStatus(TaskStatus.Finished)
    val events = Event.TaskQueued(taskWithId1) :: Event.TaskFinished(taskWithId2) :: Event.Rested :: Nil
    val projection = projectionFromEvents(events)
    projectionTestKit.run(projection) {
      assertInfo(taskInfo1)
      assertInfo(taskInfo2)
    }
  }

}
