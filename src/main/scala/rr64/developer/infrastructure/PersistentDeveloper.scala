package rr64.developer.infrastructure

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import rr64.developer.domain._
import rr64.developer.infrastructure.DeveloperBehavior.Replies

import scala.concurrent.{ExecutionContext, Future}

/**
 * Реализация поведения разработчика на основе Akka Persistence
 */
class PersistentDeveloper(ref: ActorRef[DeveloperBehavior.Command])
    (implicit timeout: Timeout, scheduler: Scheduler) extends Developer {

  /** Состояние разработчика */
  override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
    Future.successful(DeveloperState.Free)

  /** Поручить разработчику задачу */
  override def addTask(task: Task)
      (implicit ec: ExecutionContext): Future[DeveloperReply] = {
    ref.ask(DeveloperBehavior.AddTask(task, _)).map {
      case Replies.TaskStarted(id) => DeveloperReply.TaskStarted(id)
      case Replies.TaskQueued(id) => DeveloperReply.TaskQueued(id)
    }
  }

}

object PersistentDeveloper {
  def apply(ref: ActorRef[DeveloperBehavior.Command])
    (implicit timeout: Timeout, scheduler: Scheduler): PersistentDeveloper =
  new PersistentDeveloper(ref)
}
