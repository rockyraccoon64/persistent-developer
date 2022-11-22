package rr64.developer.infrastructure.task

import rr64.developer.domain.TaskStatus
import rr64.developer.infrastructure.Codec

class TaskStatusCodec extends Codec[TaskStatus, String] {
  private val InProgressStatus = "InProgress"
  private val QueuedStatus = "Queued"
  private val FinishedStatus = "Finished"

  override def encode(value: TaskStatus): String =
    value match {
      case TaskStatus.InProgress => InProgressStatus
      case TaskStatus.Queued => QueuedStatus
      case TaskStatus.Finished => FinishedStatus
    }

  override def decode(value: String): TaskStatus =
    value match {
      case InProgressStatus => TaskStatus.InProgress
      case QueuedStatus => TaskStatus.Queued
      case FinishedStatus => TaskStatus.Finished
    }

}
