package rr64.developer.domain

trait Developer {
  def state: DeveloperState
  def addTask(task: Task): DeveloperReply
}
