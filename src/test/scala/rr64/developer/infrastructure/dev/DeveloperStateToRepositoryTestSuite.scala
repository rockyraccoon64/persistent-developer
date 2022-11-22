package rr64.developer.infrastructure.dev

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
import rr64.developer.domain.Task
import rr64.developer.domain.dev.DeveloperState
import rr64.developer.infrastructure.ProjectionTestUtils
import rr64.developer.infrastructure.TaskTestUtils.TaskWithIdFactory
import rr64.developer.infrastructure.dev.behavior.Event

import scala.concurrent.{ExecutionContext, Future}

/**
 * Тесты обработчика проекции состояния разработчика
 * */
class DeveloperStateToRepositoryTestSuite
  extends ScalaTestWithActorTestKit
  with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)
  private implicit val ec: ExecutionContext = system.executionContext

  trait Fixture {

    private val mockRepository: DeveloperStateRepository = new DeveloperStateRepository {
      private var states: Map[String, DeveloperState] = Map.empty
      override def save(id: String, state: DeveloperState)(implicit ec: ExecutionContext): Future[Unit] = {
        states = states.updated(id, state)
        Future.unit
      }
      override def findById(id: String)(implicit ec: ExecutionContext): Future[Option[DeveloperState]] =
        Future.successful(states.get(id))
    }

    private val handler: Handler[EventEnvelope[Event]] = new DeveloperStateToRepository(mockRepository)

    protected def projectionFromEvents(
      events: Seq[Event],
      persistenceId: String = defaultPersistenceId
    ): TestProjection[Offset, EventEnvelope[Event]] = {
      val source = ProjectionTestUtils.envelopeSource(events, persistenceId)
      projectionFromSource(source)
    }

    protected def projectionFromSource(
      source: Source[EventEnvelope[Event], NotUsed]
    ): TestProjection[Offset, EventEnvelope[Event]] =
      TestProjection(
        projectionId = ProjectionId("dev-proj-test", "0"),
        sourceProvider = ProjectionTestUtils.providerFromSource(source),
        handler = () => handler
      )

    protected def assertState(
      state: DeveloperState,
      persistenceId: String = defaultPersistenceId,
      repository: DeveloperStateRepository = mockRepository
    ): Assertion =
      repository.findById(persistenceId).futureValue shouldEqual Some(state)

  }

  private val defaultPersistenceId = "test-id"

  private val defaultTask1 = Task(1).withRandomId
  private val defaultTask2 = Task(5).withRandomId

  /** Обработчик проекции должен обновлять состояние разработчика на "Работает", когда он начинает задачу */
  "The handler" should "update the developer state in the repository when a task is started" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) :: Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Working)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Отдых", когда он заканчивает задачу */
  "The handler" should "update the developer state in the repository when a task is finished" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished(defaultTask1) :: Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Свободен",
   * когда он отдохнул и у него нет задач */
  "The handler" should "update the state to Free after the developer rests if he has no more tasks" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished(defaultTask1) ::
      Event.Rested(None) ::
      Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Free)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Работает",
   * когда он отдохнул и в очереди есть задача */
  "The handler" should "update the state to Working after the developer rests if there is a task in the queue" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskQueued(defaultTask2) ::
      Event.TaskFinished(defaultTask1) ::
      Event.Rested(Some(defaultTask2)) ::
      Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Working)
    }
  }

  /** Обработчик проекции не должен обновлять состояние разработчика при получении задачи, когда он работает */
  "The handler" should "not update the state after receiving a new task while working" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskQueued(defaultTask2) ::
      Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Working)
    }
  }

  /** Обработчик проекции не должен обновлять состояние разработчика при получении задачи, когда он отдыхает */
  "The handler" should "not update the state when the developer receives a new task while resting" in new Fixture {
    val events = Event.TaskStarted(defaultTask1) ::
      Event.TaskFinished(defaultTask1) ::
      Event.TaskQueued(defaultTask2) ::
      Nil
    val projection = projectionFromEvents(events)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting)
    }
  }

  /** Для каждого разработчика состояние обновляется отдельно */
  "The handler" should "update states for different developers separately" in new Fixture {
    val differentPersistenceId = "test-id2"
    val events1 = ProjectionTestUtils.envelopeSource[Event](Event.TaskStarted(defaultTask2) :: Event.TaskFinished(defaultTask2) :: Nil, defaultPersistenceId)
    val events2 = ProjectionTestUtils.envelopeSource[Event](Event.TaskStarted(defaultTask1) :: Nil, differentPersistenceId, startOffset = 2)

    val projection = projectionFromSource(events1 concat events2)

    projectionTestKit.run(projection) {
      assertState(DeveloperState.Resting, defaultPersistenceId)
      assertState(DeveloperState.Working, differentPersistenceId)
    }
  }

}
