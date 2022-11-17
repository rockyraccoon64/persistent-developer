package rr64.developer.infrastructure

import rr64.developer.domain.Task
import rr64.developer.infrastructure.DeveloperBehavior.TaskWithId

import java.util.UUID

object TaskTestUtils {
  implicit class TaskWithIdFactory(task: Task) {
    def withRandomId: TaskWithId = TaskWithId(task, UUID.randomUUID())
  }
}
