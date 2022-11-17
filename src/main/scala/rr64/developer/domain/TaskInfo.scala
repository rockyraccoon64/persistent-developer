package rr64.developer.domain

import java.util.UUID

case class TaskInfo(id: UUID, difficulty: Int, status: TaskStatus)
