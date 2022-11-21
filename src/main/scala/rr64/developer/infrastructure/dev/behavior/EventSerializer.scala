package rr64.developer.infrastructure.dev.behavior

import akka.serialization.SerializerWithStringManifest
import rr64.developer.infrastructure.task.TaskWithId

import java.nio.ByteBuffer
import java.util.UUID

/**
 * Сериализатор событий актора разработчика
 */
class EventSerializer extends SerializerWithStringManifest {

  private object Manifests {
    val TaskQueued = "TaskQueued"
    val TaskStarted = "TaskStarted"
    val TaskFinished = "TaskFinished"
    val Rested = "Rested"
  }

  override def identifier: Int = 10538

  override def manifest(o: AnyRef): String = o match {
    case Event.TaskQueued(_) => Manifests.TaskQueued
    case Event.TaskStarted(_) => Manifests.TaskStarted
    case Event.TaskFinished(_) => Manifests.TaskFinished
    case Event.Rested(_) => Manifests.Rested
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case Event.TaskQueued(taskWithId) =>
      taskToByteArray(taskWithId)
    case Event.TaskStarted(taskWithId) =>
      taskToByteArray(taskWithId)
    case Event.TaskFinished(taskWithId) =>
      taskToByteArray(taskWithId)
    case Event.Rested(nextTaskOpt) =>
      nextTaskOpt.map(taskToByteArray).getOrElse(Array.empty)
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case Manifests.TaskQueued =>
      Event.TaskQueued(taskFromByteArray(bytes))
    case Manifests.TaskStarted =>
      Event.TaskStarted(taskFromByteArray(bytes))
    case Manifests.TaskFinished =>
      Event.TaskFinished(taskFromByteArray(bytes))
    case Manifests.Rested if bytes.nonEmpty =>
      Event.Rested(Some(taskFromByteArray(bytes)))
    case Manifests.Rested =>
      Event.Rested(None)
  }

  private def taskToByteArray(taskWithId: TaskWithId): Array[Byte] = {
    val id = taskWithId.id
    val difficulty = taskWithId.difficulty
    ByteBuffer.allocate(20)
      .putLong(id.getMostSignificantBits)
      .putLong(id.getLeastSignificantBits)
      .putInt(difficulty.value)
      .array
  }

  private def taskFromByteArray(bytes: Array[Byte]): TaskWithId = {
    val buffer = ByteBuffer.wrap(bytes)
    val long1 = buffer.getLong()
    val long2 = buffer.getLong()
    val difficulty = buffer.getInt()
    val id = new UUID(long1, long2)
    TaskWithId(difficulty, id)
  }

}

