package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.TaskStatus

class TaskStatusCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new TaskStatusCodec

  private class CodecTest(status: TaskStatus) {
    val encoded = codec.encode(status)
    codec.decode(encoded) shouldEqual status
  }

  "The task status codec" should {

    "encode and decode the In Progress status" in new CodecTest(TaskStatus.InProgress)

    "encode and decode the Queued status" in new CodecTest(TaskStatus.Queued)

    "encode and decode the Finished status" in new CodecTest(TaskStatus.Finished)

  }

}
