package rr64.developer.infrastructure.dev

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import rr64.developer.domain._
import rr64.developer.infrastructure.dev.DeveloperBehavior.Replies
import rr64.developer.infrastructure.dev.PersistentDeveloper.DeveloperRef

import scala.concurrent.{ExecutionContext, Future}

/**
 * Реализация поведения разработчика на основе Akka Persistence
 */
class PersistentDeveloper(
  developerRef: DeveloperRef,
  stateProvider: DeveloperStateProvider
)(implicit timeout: Timeout, scheduler: Scheduler) extends Developer {

  /** Состояние разработчика */
  override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
    stateProvider.state

  /** Поручить разработчику задачу */
  override def addTask(task: Task)
      (implicit ec: ExecutionContext): Future[DeveloperReply] = {
    developerRef.ask(DeveloperBehavior.AddTask(task, _)).map {
      case Replies.TaskStarted(id) => DeveloperReply.TaskStarted(id)
      case Replies.TaskQueued(id) => DeveloperReply.TaskQueued(id)
    }
  }

}

object PersistentDeveloper {

  type DeveloperRef = ActorRef[DeveloperBehavior.Command]

  def apply(developerRef: DeveloperRef, stateProvider: DeveloperStateProvider)
    (implicit timeout: Timeout, scheduler: Scheduler): PersistentDeveloper =
  new PersistentDeveloper(developerRef, stateProvider)

}
