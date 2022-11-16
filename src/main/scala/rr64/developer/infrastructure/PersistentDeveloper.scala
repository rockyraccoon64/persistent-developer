package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import rr64.developer.domain.DeveloperState._
import rr64.developer.domain._

import java.util.UUID

class PersistentDeveloper(ref: ActorRef[DeveloperBehavior.Command])
    (implicit timeout: Timeout, scheduler: Scheduler) extends Developer {
  override def state: DeveloperState = Free
  override def addTask(task: Task): DeveloperReply = {
    ref.ask(DeveloperBehavior.AddTask(task, _))
    DeveloperReply.TaskAccepted(UUID.randomUUID())
  }
}

object PersistentDeveloper {
  def apply(ref: ActorRef[DeveloperBehavior.Command])
    (implicit timeout: Timeout, scheduler: Scheduler): PersistentDeveloper =
  new PersistentDeveloper(ref)
}
