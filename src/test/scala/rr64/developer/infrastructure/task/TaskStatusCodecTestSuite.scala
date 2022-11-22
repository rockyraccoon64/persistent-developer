package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.TaskStatus

/**
 * Тесты кодека статусов задач
 */
class TaskStatusCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new TaskStatusCodec

  private class CodecTest(status: TaskStatus) {
    val encoded = codec.encode(status)
    codec.decode(encoded) shouldEqual status
  }

  /** Кодек должен кодировать и декодировать статус задачи */
  "The task status codec" should {

    /** "В работе" */
    "encode and decode the In Progress status" in new CodecTest(TaskStatus.InProgress)

    /** "В очереди" */
    "encode and decode the Queued status" in new CodecTest(TaskStatus.Queued)

    /** "Завершена" */
    "encode and decode the Finished status" in new CodecTest(TaskStatus.Finished)

  }

}
