package rr64.developer.infrastructure

import rr64.developer.domain.task.Task
import rr64.developer.infrastructure.task.TaskWithId

import java.util.UUID

object TaskTestUtils {

  implicit class TaskWithIdFactory(task: Task) {
    def withRandomId: TaskWithId = TaskWithId(task, UUID.randomUUID())
  }

}
