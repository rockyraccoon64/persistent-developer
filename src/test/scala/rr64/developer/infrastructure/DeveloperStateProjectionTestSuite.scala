package rr64.developer.infrastructure

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.{ProjectionTestKit, TestProjection, TestSourceProvider}
import akka.stream.scaladsl.Source
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

  /** Обработчик проекции должен обновлять состояние разработчика на "Работает", когда он начинает задачу */
  "The handler" should "update the developer state in the repository when a task is started" in {
    val persistenceId = "test-id"
    val source: Source[EventEnvelope[Event], NotUsed] =
      Source(
        EventEnvelope[Event](
          offset = Offset.sequence(0),
          persistenceId = persistenceId,
          sequenceNr = 0,
          event = Event.TaskStarted(
            TaskWithId(Task(1), UUID.randomUUID())
          ),
          timestamp = 5
        ) :: Nil
      )
    val sourceProvider = TestSourceProvider(
      source,
      (envelope: EventEnvelope[Event]) => envelope.offset
    )

    val projection = TestProjection(ProjectionId("dev-state-test", "0"), sourceProvider, () => handler)

    projectionTestKit.run(projection) {
      mockRepository.findById(persistenceId).futureValue shouldEqual Some(DeveloperState.Working)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика на "Отдых", когда он заканчивает задачу */
  "The handler" should "update the developer state in the repository when a task is finished" in {
    val persistenceId = "test-id"
    val source: Source[EventEnvelope[Event], NotUsed] =
      Source(
        EventEnvelope[Event](
          offset = Offset.sequence(0),
          persistenceId = persistenceId,
          sequenceNr = 0,
          event = Event.TaskStarted(
            TaskWithId(Task(1), UUID.randomUUID())
          ),
          timestamp = 5
        ) :: EventEnvelope[Event](
          offset = Offset.sequence(1),
          persistenceId = persistenceId,
          sequenceNr = 1,
          event = Event.TaskFinished,
          timestamp = 7
        ) :: Nil
      )
    val sourceProvider = TestSourceProvider(
      source,
      (envelope: EventEnvelope[Event]) => envelope.offset
    )

    val projection = TestProjection(ProjectionId("dev-state-test", "0"), sourceProvider, () => handler)

    projectionTestKit.run(projection) {
      mockRepository.findById(persistenceId).futureValue shouldEqual Some(DeveloperState.Resting)
    }
  }

  /** Обработчик проекции должен обновлять состояние разработчика, когда он заканчивает отдых */

  /** Обработчик проекции не должен обновлять состояние разработчика, когда оно не изменяется */

  /** Для каждого разработчика хранится своё состояние */

}
