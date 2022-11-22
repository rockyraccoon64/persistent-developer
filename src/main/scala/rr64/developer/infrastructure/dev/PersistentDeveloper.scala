package rr64.developer.infrastructure.dev

import akka.actor.typed.Scheduler
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import rr64.developer.domain._
import rr64.developer.infrastructure.dev.behavior.DeveloperBehavior.DeveloperRef
import rr64.developer.infrastructure.dev.behavior.{Command, Replies}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Разработчик на основе Akka Persistence
 * @param developerRef Ссылка на актор разработчика
 * @param stateProvider Источник состояний разработчика
 */
class PersistentDeveloper(
  developerRef: DeveloperRef,
  stateProvider: DeveloperStateProvider
)(implicit timeout: Timeout, scheduler: Scheduler) extends Developer {

  override def state(implicit ec: ExecutionContext): Future[DeveloperState] =
    stateProvider.state

  override def addTask(task: Task)
      (implicit ec: ExecutionContext): Future[DeveloperReply] = {
    developerRef.ask(Command.AddTask(task, _)).map {
      case Replies.TaskStarted(id) => DeveloperReply.TaskStarted(id)
      case Replies.TaskQueued(id) => DeveloperReply.TaskQueued(id)
    }
  }

}

object PersistentDeveloper {

  def apply(developerRef: DeveloperRef, stateProvider: DeveloperStateProvider)
    (implicit timeout: Timeout, scheduler: Scheduler): PersistentDeveloper =
  new PersistentDeveloper(developerRef, stateProvider)

}
