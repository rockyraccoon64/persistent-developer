package rr64.developer.infrastructure.task

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projection.testkit.scaladsl.ProjectionTestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpecLike
import rr64.developer.infrastructure.DeveloperBehavior.Event

import scala.concurrent.ExecutionContext

/**
 * Тесты обработчика проекции задач
 */
class TaskToRepositoryTestSuite
  extends ScalaTestWithActorTestKit
    with MockFactory
    with AnyFlatSpecLike {

  private val projectionTestKit = ProjectionTestKit(system)
  private implicit val ec: ExecutionContext = system.executionContext

  private val mockRepository: TaskRepository = mock[TaskRepository]

  private val handler: Handler[EventEnvelope[Event]] = new TaskToRepository(mockRepository)

}
