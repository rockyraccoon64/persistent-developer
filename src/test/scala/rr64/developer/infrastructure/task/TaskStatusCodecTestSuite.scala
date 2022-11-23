package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.task.TaskStatus

/**
 * Тесты кодека статусов задач
 */
class TaskStatusCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new TaskStatusCodec

  /** Проверка симметричности кодирования и декодирования */
  private def codecTest(status: TaskStatus) {
    val encoded = codec.encode(status)
    codec.decode(encoded) shouldEqual status
  }

  /** Кодек должен кодировать и декодировать статус задачи */
  "The task status codec" should {

    /** "В работе" */
    "encode and decode the In Progress status" in codecTest(TaskStatus.InProgress)

    /** "В очереди" */
    "encode and decode the Queued status" in codecTest(TaskStatus.Queued)

    /** "Завершена" */
    "encode and decode the Finished status" in codecTest(TaskStatus.Finished)

  }

}
