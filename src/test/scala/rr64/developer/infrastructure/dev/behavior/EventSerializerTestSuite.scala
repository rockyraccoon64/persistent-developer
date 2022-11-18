package rr64.developer.infrastructure.dev.behavior

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID

class EventSerializerTestSuite extends AnyFlatSpec with Matchers {

  private val serializer = new EventSerializer

  private def assertSerialized(event: Event) {
    val bytes = serializer.toBinary(event)
    val manifest = serializer.manifest(event)
    serializer.fromBinary(bytes, manifest) shouldEqual event
  }

  "The TaskQueued event" should "be serialized" in {
    val task = TaskWithId(3, UUID.randomUUID())
    val event = Event.TaskQueued(task)
    assertSerialized(event)
  }

  "The TaskStarted event" should "be serialized" in {
    val task = TaskWithId(3, UUID.randomUUID())
    val event = Event.TaskStarted(task)
    assertSerialized(event)
  }

}
