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
import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.{Event, TaskWithId}

import java.util.UUID

class DeveloperStateProjectionTestSuite
  extends ScalaTestWithActorTestKit
  with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)

  /** Обработчик проекции должен обновлять состояние разработчика, когда он начинает задачу */
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

    val mockRepo = new DeveloperStateRepository
    val handler: Handler[Event] = new DeveloperStateToRepository(mockRepo)

    val projection = TestProjection(ProjectionId("dev-state-test", "0"), sourceProvider, () => handler)

    projectionTestKit.run(projection) {
      mockRepo.findById(persistenceId).futureValue
    }
  }

}
