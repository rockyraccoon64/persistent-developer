package rr64.developer.infrastructure.task

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import rr64.developer.infrastructure.CodecTestFacade._
import rr64.developer.infrastructure.task.TaskTestFacade._

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
    "encode and decode the In Progress status" in codecTest(inProgressTaskStatus)

    /** "В очереди" */
    "encode and decode the Queued status" in codecTest(queuedTaskStatus)

    /** "Завершена" */
    "encode and decode the Finished status" in codecTest(finishedTaskStatus)

  }

}
