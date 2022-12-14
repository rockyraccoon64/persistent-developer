package rr64.developer.infrastructure.dev.behavior

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.infrastructure.facade.event.DeveloperEventTestFacade._
import rr64.developer.infrastructure.facade.task.TaskTestFacade._

/**
 * Тесты сериализации событий
 */
class EventSerializerTestSuite extends AnyFlatSpec with Matchers {

  private val serializer = new EventSerializer

  /** Проверка симметричности сериализации и десериализации */
  private def assertSerialized(event: Event): Assertion = {
    val bytes = serializer.toBinary(event)
    val manifest = serializer.manifest(event)
    serializer.fromBinary(bytes, manifest) shouldEqual event
  }

  /** Сериализация события "Задача поставлена в очередь" */
  "The Task Queued event" should "be serialized" in {
    val task = createTaskWithId(3, "3b93d086-16f1-410e-9325-4c2e220bef8f")
    val event = taskQueuedEvent(task)
    assertSerialized(event)
  }

  /** Сериализация события "Задача передана в разработку" */
  "The Task Started event" should "be serialized" in {
    val task = createTaskWithId(19, "39ea0ce2-e56f-42a8-aa4c-0a3cd496894e")
    val event = taskStartedEvent(task)
    assertSerialized(event)
  }

  /** Сериализация события "Задача завершена" */
  "The Task Finished event" should "be serialized" in {
    val task = createTaskWithId(97, "94b9c4e5-03f8-4f2c-aed9-1f1a22fd1e3b")
    val event = taskFinishedEvent(task)
    assertSerialized(event)
  }

  /** Сериализация события "Отдых завершён" с задачей в очереди */
  "The Rested event" should "be serialized when there's a next task" in {
    val task = createTaskWithId(55, "54ef15ab-8eb5-4a96-ad52-cd608b0f86f7")
    val event = restedEvent(Some(task))
    assertSerialized(event)
  }

  /** Сериализация события "Отдых завершён" без задач в очереди */
  "The Rested event" should "be serialized when there is no next task" in {
    val event = restedEvent(None)
    assertSerialized(event)
  }

}
