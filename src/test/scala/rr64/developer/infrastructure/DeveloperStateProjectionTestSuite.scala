package rr64.developer.infrastructure

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.{Handler, SourceProvider}
import akka.projection.testkit.scaladsl.{ProjectionTestKit, TestProjection, TestSourceProvider}
import akka.stream.scaladsl.Source
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.domain.{DeveloperState, Task}
import rr64.developer.infrastructure.DeveloperBehavior.{Event, TaskWithId}
import rr64.developer.infrastructure.state.{DeveloperStateRepository, DeveloperStateToRepository}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class DeveloperStateProjectionTestSuite
  extends ScalaTestWithActorTestKit
  with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)
  private implicit val ec: ExecutionContext = system.executionContext

  private val mockRepository = new DeveloperStateRepository {
    private var states: Map[String, DeveloperState] = Map.empty
    override def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[Unit] = {
      states = states.updated(id, state)
      Future.unit
    }
    override def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
      Future.successful(states.get(id))
  }

  private val handler: Handler[EventEnvelope[Event]] = new DeveloperStateToRepository(mockRepository)

  private val defaultPersistenceId = "test-id"

  private val defaultTask1 = TaskWithId(Task(1), UUID.randomUUID())
  private val defaultTask2 = TaskWithId(Task(5), UUID.randomUUID())

  private def envelopeSource(
    events: Seq[Event],
    persistenceId: String = defaultPersistenceId,
    startOffset: Long = 0
  ): Source[EventEnvelope[Event], NotUsed] =
    Source(events).zipWithIndex.map { case (event, idx) =>
      event.toEnvelope(persistenceId, startOffset + idx)
    }

  private def providerFromEnvelopeSource(
    source: Source[EventEnvelope[Event], NotUsed]
  ): TestSourceProvider[Offset, EventEnvelope[Event]] =
    TestSourceProvider(
      source,
      (envelope: EventEnvelope[Event]) => envelope.offset
    )

  private def createProjection(
    sourceProvider: SourceProvider[Offset, EventEnvelope[Event]]
  ): TestProjection[Offset, EventEnvelope[Event]] =
    TestProjection(
      projectionId = ProjectionId("dev-state-test", "0"),
      sourceProvider = sourceProvider,
      handler = () => handler
    )

  private def createProjection(
    events: Seq[Event],
    persistenceId: String = defaultPersistenceId
  ): TestProjection[Offset, EventEnvelope[Event]] = {
    val source = envelopeSource(events, persistenceId)
    projectionFromSource(source)
  }

  private def projectionFromSource(
    source: Source[EventEnvelope[Event], NotUsed]
  ): TestProjection[Offset, EventEnvelope[Event]] = {
    val sourceProvider = providerFromEnvelopeSource(source)
    createProjection(sourceProvider)
  }

  def assertState(
    state: DeveloperState,
    persistenceId: String = defaultPersistenceId,
    repository: DeveloperStateRepository = mockRepository
  ): Assertion =
    repository.findById(persistenceId).futureValue shouldEqual Some(state)

  /** Обработчик проекции должен обновлять состояние разработчика на "Работает", когда он начинает задачу */
  "The handler" should "update the developer state in the repository when a task is started" in {
    val events = Event.TaskStarted(defaultTask1) :: Nil
    val projection = createProjection(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Working)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Отдых", когда он заканчивает задачу */
  "The handler" should "update the developer state in the repository when a task is finished" in {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished :: Nil
    val projection = createProjection(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Свободен",
   * когда он отдохнул и у него нет задач */
  "The handler" should "update the state to Free after the developer rests if he has no more tasks" in {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished ::
      Event.Rested ::
      Nil
    val projection = createProjection(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Free)
    }
  }

  /** Обработчик проекции не должен обновлять состояние разработчика при получении задачи, когда он работает */
  "The handler" should "not update the state after receiving a new task while working" in {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskQueued(defaultTask2) ::
      Nil
    val projection = createProjection(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Working)
    }
  }

  /** Обработчик проекции не должен обновлять состояние разработчика при получении задачи, когда он отдыхает */
  "The handler" should "not update the state when the developer receives a new task while resting" in {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished ::
      Event.TaskQueued(defaultTask2) ::
      Nil
    val projection = createProjection(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting)
    }
  }

  /** Для каждого разработчика состояние обновляется отдельно */
  "The handler" should "update states for different developers separately" in {
    val differentPersistenceId = "test-id2"
    val events1 = envelopeSource(Event.TaskStarted(defaultTask2) :: Event.TaskFinished :: Nil)
    val events2 = envelopeSource(Event.TaskStarted(defaultTask1) :: Nil, differentPersistenceId, startOffset = 2)

    val projection = projectionFromSource(events1 concat events2)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting, defaultPersistenceId)
      assertState(DeveloperState.Working, differentPersistenceId)
    }
  }

  implicit class EventOps(evt: Event) {
    def toEnvelope(persistenceId: String, offset: Long): EventEnvelope[Event] = EventEnvelope(
      offset = Offset.sequence(offset),
      persistenceId = persistenceId,
      sequenceNr = offset,
      event = evt,
      timestamp = offset
    )
  }

}
