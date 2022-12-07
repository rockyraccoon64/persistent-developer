package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.domain.task.TaskStatus
import rr64.developer.infrastructure.CodecTestFacade._

/**
 * Тесты кодека статусов задач
 */
class TaskStatusCodecTestSuite
  extends AnyWordSpec
    with Matchers {

  private val codec = new TaskStatusCodec
  private val codecTest = assertCodecSymmetrical(codec) _

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
