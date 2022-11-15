package rr64.developer.infrastructure

import rr64.developer.domain._
import rr64.developer.domain.DeveloperState._

class PersistentDeveloper extends Developer {
  override def state: DeveloperState = Free
  override def addTask(task: Task): DeveloperReply = ???
}

object PersistentDeveloper {
  def apply(): PersistentDeveloper = new PersistentDeveloper
}
