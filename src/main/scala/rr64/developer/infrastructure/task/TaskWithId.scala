package rr64.developer.infrastructure.task

import rr64.developer.domain.Task

import java.util.UUID

case class TaskWithId(task: Task, id: UUID)

object TaskWithId {

  def fromTask(task: Task): TaskWithId =
    TaskWithId(task, UUID.randomUUID())

  def apply(difficulty: Int, id: UUID): TaskWithId =
    TaskWithId(Task(difficulty), id)

}