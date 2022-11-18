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
    val task = TaskWithId(3, UUID.fromString("3b93d086-16f1-410e-9325-4c2e220bef8f"))
    val event = Event.TaskQueued(task)
    assertSerialized(event)
  }

  "The TaskStarted event" should "be serialized" in {
    val task = TaskWithId(19, UUID.fromString("39ea0ce2-e56f-42a8-aa4c-0a3cd496894e"))
    val event = Event.TaskStarted(task)
    assertSerialized(event)
  }

}
