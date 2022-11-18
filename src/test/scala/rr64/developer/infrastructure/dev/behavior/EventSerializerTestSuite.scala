package rr64.developer.infrastructure.dev.behavior

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import rr64.developer.domain.Task
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID

class EventSerializerTestSuite extends AnyFlatSpec with Matchers {

  private val serializer = new EventSerializer

  "The TaskQueued event" should "be serialized" in {
    val task = TaskWithId(Task(3), UUID.randomUUID())
    val event = Event.TaskQueued(task)
    val bytes = serializer.toBinary(event)
    val manifest = serializer.manifest(event)
    serializer.fromBinary(bytes, manifest) shouldEqual event
  }

}
