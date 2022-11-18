package rr64.developer.infrastructure.task

import rr64.developer.domain.Task

import java.util.UUID

case class TaskWithId(task: Task, id: UUID)

object TaskWithId {
  def generateTaskId(): UUID = UUID.randomUUID()
  def createTaskWithId(task: Task): TaskWithId =
    TaskWithId(task, generateTaskId())
}