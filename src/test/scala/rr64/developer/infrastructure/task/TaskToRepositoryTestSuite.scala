package rr64.developer.infrastructure.task

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.{Handler, SourceProvider}
import akka.projection.testkit.scaladsl.{ProjectionTestKit, TestProjection, TestSourceProvider}
import akka.stream.scaladsl.Source
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.{Task, TaskInfo, TaskStatus}
import rr64.developer.infrastructure.DeveloperBehavior.{Event, TaskWithId}

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

  trait Test {

    protected val mockRepository: TaskRepository = new TaskRepository { // TODO Fixture
      private var tasks: Map[UUID, TaskInfo] = Map.empty
      override def save(taskInfo: TaskInfo): Future[_] = {
        tasks = tasks.updated(taskInfo.id, taskInfo)
        Future.unit
      }
      override def findById(id: UUID): Future[Option[TaskInfo]] =
        Future.successful(tasks.get(id))
      override def list: Future[Seq[TaskInfo]] =
        Future.successful(tasks.values.toSeq)
    }

    protected val handler: Handler[EventEnvelope[Event]] = new TaskToRepository(mockRepository)

    protected def envelopeSource(
      events: Seq[Event],
      persistenceId: String,
      startOffset: Long = 0
    ): Source[EventEnvelope[Event], NotUsed] =
      Source(events).zipWithIndex.map { case (event, idx) =>
        val offset = startOffset + idx
        EventEnvelope(
          offset = Offset.sequence(offset),
          persistenceId = persistenceId,
          sequenceNr = offset,
          event = event,
          timestamp = offset
        )
      }

    protected def providerFromSource(
      source: Source[EventEnvelope[Event], NotUsed]
    ): TestSourceProvider[Offset, EventEnvelope[Event]] =
      TestSourceProvider(
        source,
        (envelope: EventEnvelope[Event]) => envelope.offset
      )

    protected def projectionFromSourceProvider(
      sourceProvider: SourceProvider[Offset, EventEnvelope[Event]]
    ): TestProjection[Offset, EventEnvelope[Event]] =
      TestProjection(
        projectionId = ProjectionId("dev-state-test", "0"),
        sourceProvider = sourceProvider,
        handler = () => handler
      )

    protected def projectionFromEvents(
      events: Seq[Event],
      persistenceId: String
    ): TestProjection[Offset, EventEnvelope[Event]] = {
      val source = envelopeSource(events, persistenceId)
      projectionFromSource(source)
    }

    protected def projectionFromSource(
      source: Source[EventEnvelope[Event], NotUsed]
    ): TestProjection[Offset, EventEnvelope[Event]] = {
      val sourceProvider = providerFromSource(source)
      projectionFromSourceProvider(sourceProvider)
    }

  }

  /** В начале работы над задачей информация о текущем статусе должна сохраняться в репозиторий */
  "The current task state" should "be saved to the repository when the task is started" in new Test {
    val taskWithId = TaskWithId(Task(90), UUID.randomUUID())
    val taskInfo = TaskInfo(taskWithId.id, taskWithId.task.difficulty, TaskStatus.InProgress)
    val events = Event.TaskStarted(taskWithId) :: Nil
    val projection: TestProjection[Offset, EventEnvelope[Event]] = projectionFromEvents(events, "proj")
    projectionTestKit.run(projection) {
      mockRepository.findById(taskInfo.id).futureValue shouldEqual Some(taskInfo)
    }
  }

}
